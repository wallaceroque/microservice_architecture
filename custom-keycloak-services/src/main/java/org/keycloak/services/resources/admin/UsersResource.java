/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.keycloak.services.resources.admin;

import org.jboss.logging.Logger;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.jboss.resteasy.spi.NotFoundException;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.keycloak.services.resources.admin.permissions.AdminPermissionEvaluator;
import org.keycloak.common.ClientConnection;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ModelDuplicateException;
import org.keycloak.models.ModelException;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.models.utils.RepresentationToModel;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.services.ErrorResponse;
import org.keycloak.services.*;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Base resource for managing users
 *
 * @resource Users
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class UsersResource {
    private static final Logger logger = Logger.getLogger(UsersResource.class);

    protected RealmModel realm;

    private AdminPermissionEvaluator auth;

    private AdminEventBuilder adminEvent;

    @Context
    protected ClientConnection clientConnection;

    @Context
    protected UriInfo uriInfo;

    @Context
    protected KeycloakSession session;

    @Context
    protected HttpHeaders headers;

    public UsersResource(RealmModel realm, AdminPermissionEvaluator auth, AdminEventBuilder adminEvent) {
        this.auth = auth;
        this.realm = realm;
        this.adminEvent = adminEvent.resource(ResourceType.USER);
    }

    /**
     * Create a new user
     *
     * Username must be unique.
     *
     * @param uriInfo
     * @param rep
     * @return
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createUser(final @Context UriInfo uriInfo, final UserRepresentation rep) {
        auth.users().requireManage();

        // Double-check duplicated username and email here due to federation
        if (session.users().getUserByUsername(rep.getUsername(), realm) != null) {
            return ErrorResponse.exists("User exists with same username");
        }
        if (rep.getEmail() != null && !realm.isDuplicateEmailsAllowed() && session.users().getUserByEmail(rep.getEmail(), realm) != null) {
            return ErrorResponse.exists("User exists with same email");
        }

        try {
            UserModel user = session.users().addUser(realm, rep.getUsername());
            Set<String> emptySet = Collections.emptySet();

            UserResource.updateUserFromRep(user, rep, emptySet, realm, session, false);
            RepresentationToModel.createCredentials(rep, session, realm, user, true);
            adminEvent.operation(OperationType.CREATE).resourcePath(uriInfo, user.getId()).representation(rep).success();

            if (session.getTransactionManager().isActive()) {
                session.getTransactionManager().commit();
            }

            return Response.created(uriInfo.getAbsolutePathBuilder().path(user.getId()).build()).build();
        } catch (ModelDuplicateException e) {
            if (session.getTransactionManager().isActive()) {
                session.getTransactionManager().setRollbackOnly();
            }
            return ErrorResponse.exists("User exists with same username or email");
        } catch (ModelException me){
            if (session.getTransactionManager().isActive()) {
                session.getTransactionManager().setRollbackOnly();
            }
            logger.warn("Could not create user", me);
            return ErrorResponse.exists("Could not create user");
        }
    }
    /**
     * Get representation of the user
     *
     * @param id User id
     * @return
     */
    @Path("{id}")
    public UserResource user(final @PathParam("id") String id) {
        UserModel user = session.users().getUserById(id, realm);
        if (user == null) {
            // we do this to make sure somebody can't phish ids
            if (auth.users().canQuery()) throw new NotFoundException("User not found");
            else throw new ForbiddenException();
        }
        UserResource resource = new UserResource(realm, user, auth, adminEvent);
        ResteasyProviderFactory.getInstance().injectProperties(resource);
        //resourceContext.initResource(users);
        return resource;
    }

    /**
     * Get users
     *
     * Returns a list of users, filtered according to query parameters
     *
     * @param search A String contained in username, first or last name, or email
     * @param last
     * @param first
     * @param email
     * @param username
     * @param first Pagination offset
     * @param maxResults Maximum results size (defaults to 100)
     * @return
     */
    @GET
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    public List<UserRepresentation> getUsers(@QueryParam("search") String search,
                                             @QueryParam("lastName") String last,
                                             @QueryParam("firstName") String first,
                                             @QueryParam("email") String email,
                                             @QueryParam("username") String username,
                                             @QueryParam("first") Integer firstResult,
                                             @QueryParam("max") Integer maxResults) {
        auth.users().requireQuery();

        firstResult = firstResult != null ? firstResult : -1;
        maxResults = maxResults != null ? maxResults : Constants.DEFAULT_MAX_RESULTS;

        List<UserRepresentation> results = new ArrayList<UserRepresentation>();
        List<UserModel> userModels;
        if (search != null) {
            userModels = session.users().searchForUser(search.trim(), realm, firstResult, maxResults);
        } else if (last != null || first != null || email != null || username != null) {
            Map<String, String> attributes = new HashMap<String, String>();
            if (last != null) {
                attributes.put(UserModel.LAST_NAME, last);
            }
            if (first != null) {
                attributes.put(UserModel.FIRST_NAME, first);
            }
            if (email != null) {
                attributes.put(UserModel.EMAIL, email);
            }
            if (username != null) {
                attributes.put(UserModel.USERNAME, username);
            }
            userModels = session.users().searchForUser(attributes, realm, firstResult, maxResults);
        } else {
            userModels = session.users().getUsers(realm, firstResult, maxResults, false);
        }

        boolean canViewGlobal = auth.users().canView();
        for (UserModel user : userModels) {
            if (!canViewGlobal  && !auth.users().canView(user)) continue;
            UserRepresentation userRep = ModelToRepresentation.toRepresentation(session, realm, user);
            userRep.setAccess(auth.users().getAccess(user));
            results.add(userRep);
        }
        return results;
    }

    @Path("count")
    @GET
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    public Integer getUsersCount() {
        auth.users().requireView();

        return session.users().getUsersCount(realm);
    }
}
