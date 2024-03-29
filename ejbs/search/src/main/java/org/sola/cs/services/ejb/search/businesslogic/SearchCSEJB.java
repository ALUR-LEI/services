/**
 * ******************************************************************************************
 * Copyright (C) 2014 - Food and Agriculture Organization of the United Nations
 * (FAO). All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,this
 * list of conditions and the following disclaimer. 2. Redistributions in binary
 * form must reproduce the above copyright notice,this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution. 3. Neither the name of FAO nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT,STRICT LIABILITY,OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
 * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * *********************************************************************************************
 */
package org.sola.cs.services.ejb.search.businesslogic;

import org.sola.cs.services.ejb.search.repository.entities.ApplicationSearchParams;
import org.sola.cs.services.ejb.search.repository.entities.ClaimSpatialSearchResult;
import org.sola.cs.services.ejb.search.repository.entities.PartySearchParams;
import org.sola.cs.services.ejb.search.repository.entities.ConfigMapLayer;
import org.sola.cs.services.ejb.search.repository.entities.NotifiablePartySearchResult;
import org.sola.cs.services.ejb.search.repository.entities.ApplicationSearchResult;
import org.sola.cs.services.ejb.search.repository.entities.UserSearchResult;
import org.sola.cs.services.ejb.search.repository.entities.ApplicationLogResult;
import org.sola.cs.services.ejb.search.repository.entities.RightsExportParams;
import org.sola.cs.services.ejb.search.repository.entities.BaUnitSearchParams;
import org.sola.cs.services.ejb.search.repository.entities.BaUnitSearchResult;
import org.sola.cs.services.ejb.search.repository.entities.SpatialResult;
import org.sola.cs.services.ejb.search.repository.entities.SourceSearchParams;
import org.sola.cs.services.ejb.search.repository.entities.UserSearchParams;
import org.sola.cs.services.ejb.search.repository.entities.BrSearchParams;
import org.sola.cs.services.ejb.search.repository.entities.GenericResult;
import org.sola.cs.services.ejb.search.repository.entities.DynamicQueryField;
import org.sola.cs.services.ejb.search.repository.entities.RightsExportResult;
import org.sola.cs.services.ejb.search.repository.entities.Setting;
import org.sola.cs.services.ejb.search.repository.entities.SpatialSearchResult;
import org.sola.cs.services.ejb.search.repository.entities.ClaimSearchParams;
import org.sola.cs.services.ejb.search.repository.entities.ClaimSpatialSearchParams;
import org.sola.cs.services.ejb.search.repository.entities.PowerOfAttorneySearchResult;
import org.sola.cs.services.ejb.search.repository.entities.Crs;
import org.sola.cs.services.ejb.search.repository.entities.PartyPropertySearchResult;
import org.sola.cs.services.ejb.search.repository.entities.PowerOfAttorneySearchParams;
import org.sola.cs.services.ejb.search.repository.entities.ClaimSearchResult;
import org.sola.cs.services.ejb.search.repository.entities.PropertyVerifier;
import org.sola.cs.services.ejb.search.repository.entities.PartySearchResult;
import org.sola.cs.services.ejb.search.repository.entities.DynamicQuery;
import org.sola.cs.services.ejb.search.repository.entities.SourceSearchResult;
import org.sola.cs.services.ejb.search.repository.entities.SpatialSearchOption;
import org.sola.cs.services.ejb.search.repository.entities.BrSearchResult;
import java.util.*;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.apache.commons.lang.math.NumberUtils;
import org.sola.common.ClaimStatusConstants;
import org.sola.common.DateUtility;
import org.sola.common.RolesConstants;
import org.sola.common.SOLAException;
import org.sola.common.StringUtility;
import org.sola.cs.common.messaging.ServiceMessage;
import org.sola.services.common.ejbs.AbstractEJB;
import org.sola.services.common.repository.CommonSqlProvider;
import org.sola.services.common.repository.entities.AbstractReadOnlyEntity;
import org.sola.cs.services.ejb.search.repository.SearchSqlProvider;
import org.sola.cs.services.ejb.search.repository.entities.AdministrativeBoundarySearchResult;
import org.sola.cs.services.ejb.search.repository.entities.AdministrativeBoundaryWithGeomSearchResult;
import org.sola.cs.services.ejb.search.repository.entities.GeoJsonAdministrativeBoundary;
import org.sola.cs.services.ejb.search.repository.entities.GeoJsonClaim;
import org.sola.cs.services.ejb.search.repository.entities.MapSearchResult;
import org.sola.cs.services.ejb.search.repository.entities.PublicDisplaySearchResult;
import org.sola.cs.services.ejb.search.spatial.QueryForNavigation;
import org.sola.cs.services.ejb.search.spatial.QueryForPublicDisplayMap;
import org.sola.cs.services.ejb.search.spatial.QueryForSelect;
import org.sola.cs.services.ejb.search.spatial.ResultForNavigationInfo;
import org.sola.cs.services.ejb.search.spatial.ResultForSelectionInfo;
import org.sola.cs.services.ejb.search.repository.entities.SpatialUnitWithGeomSearchResult;

/**
 * SOLA EJB's have responsibility for managing data in one schema. This can
 * complicate searches that require interrogating data across multiple schemas
 * to obtain a result. If the strict rule to only allow EJBs to manage data in
 * one schema was applied, cross schema searches would require the use of
 * multiple EJBs to obtain several part datasets which would then need to be
 * sorted and filtered based on the users search criteria. That approach is very
 * inefficient compared to using SQL, so the SearchEJB has been created to allow
 * efficient searching for data across multiple schemas.
 *
 * <p>
 * The SearchEJB supports execution of dynamic SQL queries obtained from the
 * system.query table.</p>
 *
 * <p>
 * Note that this EJB has access to all SOLA database tables and it must be
 * treated as read only. It must not be used to persist data changes.</p>
 */
@Stateless
@EJB(name = "java:app/SearchCSEJBLocal", beanInterface = SearchCSEJBLocal.class)
public class SearchCSEJB extends AbstractEJB implements SearchCSEJBLocal {

    /**
     * Retrieves the SQL for the dynamic query from the system.query table
     *
     * @param queryName The name of the dynamic query to retrieve
     * @param params The parameters to use for the dynamic query. If the
     * {@linkplain CommonSqlProvider#PARAM_LANGUAGE_CODE} param is supplied,
     * this value is used to localize the display values for the dynamic query.
     * @throws SOLAException If the dynamic query name does not match any query
     * in the database
     */
    private DynamicQuery getDynamicQuery(String queryName, Map params) {

        // DynamicQuery is cachable so retrieve the entire list so they are loaded into
        // the cache. 
        List<DynamicQuery> queries = getRepository().getEntityList(DynamicQuery.class, params);
        DynamicQuery result = null;
        for (DynamicQuery q : queries) {
            if (q.getName().equals(queryName)) {
                result = q;
                break;
            }
        }
        if (result == null) {
            // Raise an error to indicate the dynamic query does not exist
            throw new SOLAException(ServiceMessage.GENERAL_UNEXPECTED,
                    new Object[]{"Dynamic query " + queryName + " does not exist."});
        }
        return result;
    }

    /**
     * Returns the result obtained from executing the dynamic query.
     *
     * @param query The dynamic query to execute
     * @param params The parameters to pass to the dynamic query.
     * @return A list of HashMap(String,Object) where the String is the column
     * name and the Object is the value for the column. Each result row is
     * captured as a Hash Map in the list.
     * @see
     * org.sola.services.common.repository.CommonRepositoryImpl#mapToEntityList(java.lang.Class,
     * java.util.ArrayList) CommonRepositoryImpl.mapToEntityList
     */
    private ArrayList<HashMap> executeDynamicQuery(DynamicQuery query, Map params) {
        params = params == null ? new HashMap<String, Object>() : params;
        params.put(CommonSqlProvider.PARAM_QUERY, query.getSql());
        return getRepository().executeSql(params);
    }

    // 
    /**
     * Overloaded version of {@linkplain #executeDynamicQuery(org.sola.services.ejb.search.repository.entities.DynamicQuery,
     * java.util.Map) executeDynamicQuery} that maps the generic result from the
     * dynamic query onto an entity list.
     *
     * @param <T> Generic data type
     * @param entityClass The entity class to map for each row of the result.
     * Must extend {@linkplain AbstractReadOnlyEntity}.
     * @param queryName The name of the query to execute
     * @param params The parameters to use when executing the dynamic query.
     */
    private <T extends AbstractReadOnlyEntity> List<T> executeDynamicQuery(Class<T> entityClass,
            String queryName, Map params) {
        params = params == null ? new HashMap<String, Object>() : params;
        DynamicQuery query = getDynamicQuery(queryName, params);
        params.put(CommonSqlProvider.PARAM_QUERY, query.getSql());
        return getRepository().getEntityList(entityClass, params);
    }

    /**
     * Executes a dynamic query and returns a generic result that also provides
     * a localized display name for each result field.
     *
     * @param queryName The name of the dynamic query to execute
     * @param params The parameters to use for the dynamic query
     * @throws SOLAException If the dynamic query does not have any field
     * configuration in the system.query_fields table.
     * @see
     * #executeDynamicQuery(org.sola.services.ejb.search.repository.entities.DynamicQuery,
     * java.util.Map) executeDynamicQuery
     */
    @Override
    public GenericResult getGenericResultList(
            String queryName, Map params) {

        GenericResult result = new GenericResult();
        DynamicQuery query = getDynamicQuery(queryName, params);
        if (query.getFieldList() == null || query.getFieldList().isEmpty()) {
            throw new SOLAException(ServiceMessage.GENERAL_UNEXPECTED,
                    new Object[]{"Field list for dynamic query " + queryName + " is missing."});
        }
        ArrayList<HashMap> queryResult = executeDynamicQuery(query, params);

        // Create the generic result from the query result. 
        if (queryResult != null && !queryResult.isEmpty()) {

            String[] fieldNames;
            List<String> queryFields;
            List<String> displayNames;

            // Get any query fields and display names from the dynamic query configuration. 
            // asList returns a fixed length list backed by the array so need to create a 
            // new list based on the array instead. 
            queryFields = new ArrayList<String>(Arrays.asList(query.getQueryFieldNames()));
            displayNames = new ArrayList<String>(Arrays.asList(query.getFieldDisplayNames()));

            fieldNames = queryFields.toArray(new String[0]);
            result.setFieldNames(displayNames.toArray(new String[0]));

            // Cycle through the hash map results and capture the values for each column
            for (HashMap map : queryResult) {
                Object[] values = new Object[fieldNames.length];
                for (int i = 0; i < fieldNames.length; i++) {
                    values[i] = map.get(fieldNames[i]);
                }
                result.addRow(values);
            }
        }
        return result;
    }

    /**
     * Returns the first row of the result set obtained from the execution of a
     * dynamic SQL statement. Used for the execution of business rules.
     *
     * @param sqlStatement The SQL statement to execute
     * @param params The parameters for the SQL statement
     */
    @Override
    public HashMap getResultObjectFromStatement(String sqlStatement, Map params) {
        params = params == null ? new HashMap<String, Object>() : params;
        params.put(CommonSqlProvider.PARAM_QUERY, sqlStatement);
        // Returns a single result
        //return getRepository().getScalar(Object.class, params); 
        // To use if more than one result is required. 
        List<HashMap> resultList = getRepository().executeSql(params);
        HashMap result = null;
        if (!resultList.isEmpty()) {
            result = resultList.get(0);
        }
        return result;
    }

    /**
     * Determines if the property details provided already exist in the SOLA
     * database or not. Can be used to help determine if the property being
     * added to a new application is valid or not.
     *
     * @param applicationNumber The number of the application the property is
     * being added/associated to. Used to exclude the current application from
     * the test and avoid a false positive match.
     * @param firstPart The first part of the property name.
     * @param lastPart The last part of the property name.
     */
    @Override
    public PropertyVerifier getPropertyVerifier(String applicationNumber, String firstPart, String lastPart) {
        if (applicationNumber == null) {
            applicationNumber = "";
        }
        Map params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_QUERY, PropertyVerifier.QUERY_VERIFY_SQL);
        params.put(PropertyVerifier.QUERY_PARAM_APPLICATION_NUMBER, applicationNumber);
        params.put(PropertyVerifier.QUERY_PARAM_FIRST_PART, firstPart);
        params.put(PropertyVerifier.QUERY_PARAM_LAST_PART, lastPart);
        return getRepository().getEntity(PropertyVerifier.class, params);
    }

    /**
     * Executes a search across all applications using the search criteria
     * provided. Partial, case insensitive matches are supported for the contact
     * person name, agent name, application number, document number and the
     * document reference number criteria.
     *
     * <p>
     * Requires the {@linkplain RolesConstants#APPLICATION_VIEW_APPS} role.</p>
     *
     * @param params The criteria to use for the search.
     * @return A maximum of 100 applications that match the search criteria,
     * sorted by lodgement date DESC.
     */
    @Override
    @RolesAllowed(RolesConstants.APPLICATION_VIEW_APPS)
    public List<ApplicationSearchResult> searchApplications(ApplicationSearchParams params) {
        // Process params

        Map queryParams = new HashMap<String, Object>();
        queryParams.put(CommonSqlProvider.PARAM_FROM_PART, ApplicationSearchResult.QUERY_FROM);

        queryParams.put(CommonSqlProvider.PARAM_LANGUAGE_CODE, params.getLocale());
        queryParams.put(ApplicationSearchResult.QUERY_PARAM_CONTACT_NAME,
                params.getContactPerson() == null ? "" : params.getContactPerson().trim());
        queryParams.put(ApplicationSearchResult.QUERY_PARAM_AGENT_NAME,
                params.getAgent() == null ? "" : params.getAgent().trim());
        queryParams.put(ApplicationSearchResult.QUERY_PARAM_APP_NR,
                params.getNr() == null ? "" : params.getNr().trim());
        queryParams.put(ApplicationSearchResult.QUERY_PARAM_FROM_LODGE_DATE,
                params.getFromDate() == null ? new GregorianCalendar(1, 1, 1).getTime() : params.getFromDate());
        queryParams.put(ApplicationSearchResult.QUERY_PARAM_TO_LODGE_DATE,
                params.getToDate() == null ? new GregorianCalendar(2500, 1, 1).getTime() : params.getToDate());
        queryParams.put(ApplicationSearchResult.QUERY_PARAM_DOCUMENT_NUMBER,
                params.getDocumentNumber() == null ? "" : params.getDocumentNumber().trim());
        queryParams.put(ApplicationSearchResult.QUERY_PARAM_DOCUMENT_REFERENCE,
                params.getDocumentReference() == null ? "" : params.getDocumentReference().trim());

        queryParams.put(CommonSqlProvider.PARAM_WHERE_PART, ApplicationSearchResult.QUERY_WHERE_SEARCH_APPLICATIONS);
        queryParams.put(CommonSqlProvider.PARAM_ORDER_BY_PART, ApplicationSearchResult.QUERY_ORDER_BY);
        queryParams.put(CommonSqlProvider.PARAM_LIMIT_PART, "100");

        return getRepository().getEntityList(ApplicationSearchResult.class, queryParams);
    }

    /**
     * Executes a search across all sources using the search criteria provided.
     * Partial matches are supported for the document number and the document
     * reference number criteria.
     *
     * <p>
     * Requires the {@linkplain RolesConstants#SOURCE_SEARCH} role.</p>
     *
     * @param searchParams The criteria to use for the search.
     * @return A maximum of 101 sources that match the search criteria.
     */
    @Override
    @RolesAllowed(RolesConstants.SOURCE_SEARCH)
    public List<SourceSearchResult> searchSources(SourceSearchParams searchParams) {
        Map params = processSourceSearchParams(searchParams);
        params.put(CommonSqlProvider.PARAM_QUERY, SourceSearchResult.SEARCH_QUERY);
        return getRepository().getEntityList(SourceSearchResult.class, params);
    }

    private Map<String, Object> processSourceSearchParams(SourceSearchParams searchParams) {
        Map params = new HashMap<String, Object>();
        params.put(SourceSearchResult.QUERY_PARAM_FROM_RECORDATION_DATE,
                searchParams.getFromRecordationDate() == null
                        ? new GregorianCalendar(1, 1, 1).getTime()
                        : searchParams.getFromRecordationDate());
        params.put(SourceSearchResult.QUERY_PARAM_TO_RECORDATION_DATE,
                searchParams.getToRecordationDate() == null
                        ? new GregorianCalendar(2500, 1, 1).getTime()
                        : searchParams.getToRecordationDate());
        params.put(SourceSearchResult.QUERY_PARAM_FROM_SUBMISSION_DATE,
                searchParams.getFromSubmissionDate() == null
                        ? new GregorianCalendar(1, 1, 1).getTime()
                        : searchParams.getFromSubmissionDate());
        params.put(SourceSearchResult.QUERY_PARAM_TO_SUBMISSION_DATE,
                searchParams.getToSubmissionDate() == null
                        ? new GregorianCalendar(2500, 1, 1).getTime()
                        : searchParams.getToSubmissionDate());
        params.put(SourceSearchResult.QUERY_PARAM_TYPE_CODE,
                searchParams.getTypeCode() == null ? "" : searchParams.getTypeCode());
        params.put(SourceSearchResult.QUERY_PARAM_REF_NUMBER,
                searchParams.getRefNumber() == null ? "" : searchParams.getRefNumber());
        params.put(SourceSearchResult.QUERY_PARAM_LA_NUMBER,
                searchParams.getLaNumber() == null ? "" : searchParams.getLaNumber());
        params.put(CommonSqlProvider.PARAM_LANGUAGE_CODE,
                searchParams.getLocale() == null ? "en" : searchParams.getLocale());
        params.put(SourceSearchResult.QUERY_PARAM_OWNER_NAME,
                searchParams.getOwnerName() == null ? "" : searchParams.getOwnerName());
        params.put(SourceSearchResult.QUERY_PARAM_DESCRIPTION,
                searchParams.getDescription() == null ? "" : searchParams.getDescription());
        params.put(SourceSearchResult.QUERY_PARAM_VERSION,
                searchParams.getVersion() == null ? "" : searchParams.getVersion());
        return params;
    }

    /**
     * Executes a search across all power of attorney using the search criteria
     * provided. Partial matches are supported for the document number and the
     * document reference number criteria.
     *
     * <p>
     * Requires the {@linkplain RolesConstants#SOURCE_SEARCH} role.</p>
     *
     * @param searchParams The criteria to use for the search.
     * @return A maximum of 101 sources that match the search criteria.
     */
    @Override
    @RolesAllowed(RolesConstants.SOURCE_SEARCH)
    public List<PowerOfAttorneySearchResult> searchPowerOfAttorney(PowerOfAttorneySearchParams searchParams) {
        Map params = processSourceSearchParams(searchParams);
        params.put(PowerOfAttorneySearchResult.QUERY_PARAM_PERSON_NAME,
                searchParams.getPersonName() == null ? "" : searchParams.getPersonName());
        params.put(PowerOfAttorneySearchResult.QUERY_PARAM_ATTORNEY_NAME,
                searchParams.getAttorneyName() == null ? "" : searchParams.getAttorneyName());
        params.put(CommonSqlProvider.PARAM_QUERY, PowerOfAttorneySearchResult.SEARCH_POWER_OF_ATTORNEY_QUERY);
        return getRepository().getEntityList(PowerOfAttorneySearchResult.class, params);
    }

    /**
     * Executes a search across all users using the search criteria provided.
     * Partial matches are supported for the username, first name and last name
     * criteria.
     *
     * <p>
     * Requires the {@linkplain RolesConstants#ADMIN_MANAGE_SECURITY} role.</p>
     *
     * @param searchParams The criteria to use for the search.
     * @return The users that match the search criteria.
     */
    @Override
    @RolesAllowed(RolesConstants.ADMIN_MANAGE_SECURITY)
    public List<UserSearchResult> searchUsers(UserSearchParams searchParams) {
        if (searchParams.getGroupId() == null) {
            searchParams.setGroupId("");
        }

        if (searchParams.getUserName() == null) {
            searchParams.setUserName("");
        }

        if (searchParams.getFirstName() == null) {
            searchParams.setFirstName("");
        }

        if (searchParams.getLastName() == null) {
            searchParams.setLastName("");
        }

        Map params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_QUERY, UserSearchResult.QUERY_ADVANCED_USER_SEARCH);
        params.put("userName", searchParams.getUserName());
        params.put("firstName", searchParams.getFirstName());
        params.put("lastName", searchParams.getLastName());
        params.put("groupId", searchParams.getGroupId());
        return getRepository().getEntityList(UserSearchResult.class, params);
    }

    /**
     * Returns applications that have a lodged or approve status and are not
     * assigned to any user.
     *
     * <p>
     * Requires the {@linkplain RolesConstants#APPLICATION_VIEW_APPS} role.</p>
     *
     * @param locale The language code to use for localization of display
     * values.
     * @return A maximum of 100 applications that match the search criteria,
     * sorted by lodgement date DESC.
     */
    @Override
    @RolesAllowed(RolesConstants.DASHBOARD_VIEW_UNASSIGNED_APPS)
    public List<ApplicationSearchResult> getUnassignedApplications(String locale) {

        Map params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_FROM_PART, ApplicationSearchResult.QUERY_FROM);
        params.put(CommonSqlProvider.PARAM_LANGUAGE_CODE, locale);
        params.put(CommonSqlProvider.PARAM_WHERE_PART, ApplicationSearchResult.QUERY_WHERE_GET_UNASSIGNED);
        params.put(CommonSqlProvider.PARAM_ORDER_BY_PART, ApplicationSearchResult.QUERY_ORDER_BY);
//        params.put(CommonSqlProvider.PARAM_LIMIT_PART, "100");

        return getRepository().getEntityList(ApplicationSearchResult.class, params);
    }

    /**
     * Returns applications that have a lodged or approved status and are
     * assigned to the currently logged in user.
     *
     * <p>
     * If the currently logged in user has the
     * {@linkplain RolesConstants#APPLICATION_UNASSIGN_FROM_OTHERS} then all
     * lodged or approved applications assigned to any user are returned. </p>
     *
     * <p>
     * Requires the {@linkplain RolesConstants#APPLICATION_VIEW_APPS} role.</p>
     *
     * @param locale The language code to use for localization of display
     * values.
     * @return A maximum of 100 applications that match the search criteria,
     * sorted by lodgement date DESC.
     */
    @Override
    @RolesAllowed(RolesConstants.DASHBOARD_VIEW_ASSIGNED_APPS)
    public List<ApplicationSearchResult> getAssignedApplications(String locale) {
        Map params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_FROM_PART, ApplicationSearchResult.QUERY_FROM);
        params.put(CommonSqlProvider.PARAM_LANGUAGE_CODE, locale);

        if (isInRole(RolesConstants.APPLICATION_UNASSIGN_FROM_OTHERS)) {
            params.put(CommonSqlProvider.PARAM_WHERE_PART, ApplicationSearchResult.QUERY_WHERE_GET_ASSIGNED_ALL);
        } else {
            params.put(ApplicationSearchResult.QUERY_PARAM_USER_NAME, getUserName());
            params.put(CommonSqlProvider.PARAM_WHERE_PART, ApplicationSearchResult.QUERY_WHERE_GET_ASSIGNED);
        }

        params.put(CommonSqlProvider.PARAM_ORDER_BY_PART, ApplicationSearchResult.QUERY_ORDER_BY);
//        params.put(CommonSqlProvider.PARAM_LIMIT_PART, "100");

        return getRepository().getEntityList(ApplicationSearchResult.class, params);
    }

    /**
     * Returns details for all users marked as active in the SOLA database.
     */
    @Override
    public List<UserSearchResult> getActiveUsers() {
        Map params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_QUERY, UserSearchResult.QUERY_ACTIVE_USERS);
        return getRepository().getEntityList(UserSearchResult.class, params);
    }

    /**
     * Executes a search across all parties using the search criteria provided.
     * Partial matches are supported for the party name criteria.
     *
     * @param searchParams The criteria to use for the search.
     * @return A maximum of 101 parties that match the search criteria.
     */
    @Override
    public List<PartySearchResult> searchParties(PartySearchParams searchParams) {
        if (searchParams.getName() == null) {
            searchParams.setName("");
        }
        if (searchParams.getTypeCode() == null) {
            searchParams.setTypeCode("");
        }
        if (searchParams.getRoleTypeCode() == null) {
            searchParams.setRoleTypeCode("");
        }

        searchParams.setName(searchParams.getName().trim());

        Map params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_LANGUAGE_CODE, searchParams.getLocale());
        params.put(CommonSqlProvider.PARAM_QUERY, PartySearchResult.SEARCH_QUERY);
        params.put(PartySearchResult.QUERY_PARAM_NAME, searchParams.getName());
        params.put(PartySearchResult.QUERY_PARAM_TYPE_CODE, searchParams.getTypeCode());
        params.put(PartySearchResult.QUERY_PARAM_ROLE_TYPE_CODE, searchParams.getRoleTypeCode());
        return getRepository().getEntityList(PartySearchResult.class, params);
    }

    /**
     * Executes a search across all parties using the search criteria provided.
     * Partial matches are supported for the party name criteria.
     *
     * @param searchParams The criteria to use for the search.
     * @return A maximum of 101 parties that match the search criteria.
     */
    @Override
    public List<PartyPropertySearchResult> searchPartiesProperty(PartySearchParams searchParams, String partyId) {
        if (searchParams.getName() == null) {
            searchParams.setName("");
        }
        if (searchParams.getTypeCode() == null) {
            searchParams.setTypeCode("");
        }
        if (searchParams.getRoleTypeCode() == null) {
            searchParams.setRoleTypeCode("");
        }

        searchParams.setName(searchParams.getName().trim());

        Map params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_LANGUAGE_CODE, searchParams.getLocale());
        params.put(CommonSqlProvider.PARAM_QUERY, PartyPropertySearchResult.SEARCH_QUERY);
        params.put(PartyPropertySearchResult.QUERY_PARAM_NAME, searchParams.getName());
        params.put(PartyPropertySearchResult.QUERY_PARAM_TYPE_CODE, searchParams.getTypeCode());
        params.put(PartyPropertySearchResult.QUERY_PARAM_ROLE_TYPE_CODE, searchParams.getRoleTypeCode());
        params.put(PartyPropertySearchResult.QUERY_PARAM_PARTY_ID, partyId);
        return getRepository().getEntityList(PartyPropertySearchResult.class, params);
    }

    /**
     * Executes a search across all parties using the search criteria provided.
     * Partial matches are supported for the party name criteria.
     *
     * @param searchParams The criteria to use for the search.
     * @return A maximum of 101 parties that match the search criteria.
     */
    @Override
    public List<NotifiablePartySearchResult> searchNotifiableParties(PartySearchParams searchParams, String service) {
        if (searchParams.getName() == null) {
            searchParams.setName("");
        }

        searchParams.setName(searchParams.getName().trim());

        Map params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_LANGUAGE_CODE, searchParams.getLocale());
        params.put(CommonSqlProvider.PARAM_QUERY, NotifiablePartySearchResult.SEARCH_QUERY);
        params.put(NotifiablePartySearchResult.QUERY_PARAM_NAME, searchParams.getName());
        params.put(NotifiablePartySearchResult.QUERY_PARAM_SERVICE, service);
        return getRepository().getEntityList(NotifiablePartySearchResult.class, params);
    }

    /**
     * Used for navigation (i.e. pan and zoom) of the Map. Executes a dynamic
     * layer query using the bounding box details provided in the search
     * parameters. The dynamic query to execute must be one of the layer queries
     * in system.query.
     *
     * @param spatialQuery The parameters to use for the query including the
     * name of the dynamic layer query to execute.
     * @return A summary of all spatial objects intersecting the bounding box
     */
    @Override
    public ResultForNavigationInfo getSpatialResult(
            QueryForNavigation spatialQuery) {
        Map params = this.getSpatialNavigationQueryParams(spatialQuery);
        return getSpatialResultForNavigation(spatialQuery.getQueryName(), params);
    }

    /**
     * Used for retrieving the features of the layers used for public display.
     * It sets an extra parameter in the query used to retrieve features.
     *
     * @param spatialQuery The parameters to use for the query including the
     * name of the dynamic layer query to execute.
     * @return A summary of all spatial objects intersecting the bounding box
     */
    @Override
    public ResultForNavigationInfo getSpatialResultForPublicDisplay(
            QueryForPublicDisplayMap spatialQuery) {
        Map params = this.getSpatialNavigationQueryParams(spatialQuery);
        params.put("name_lastpart", spatialQuery.getNameLastPart());
        return getSpatialResultForNavigation(spatialQuery.getQueryName(), params);
    }

    private Map getSpatialNavigationQueryParams(QueryForNavigation spatialQuery) {
        Map params = new HashMap<String, Object>();
        params.put("minx", spatialQuery.getWest());
        params.put("miny", spatialQuery.getSouth());
        params.put("maxx", spatialQuery.getEast());
        params.put("maxy", spatialQuery.getNorth());
        params.put("srid", spatialQuery.getSrid());
        params.put("pixel_res", spatialQuery.getPixelResolution());
        return params;
    }

    private ResultForNavigationInfo getSpatialResultForNavigation(
            String queryName, Map params) {
        ResultForNavigationInfo spatialResultInfo = new ResultForNavigationInfo();
        getRepository().setLoadInhibitors(new Class[]{DynamicQueryField.class});
        List<SpatialResult> result = executeDynamicQuery(SpatialResult.class,
                queryName, params);
        getRepository().clearLoadInhibitors();
        spatialResultInfo.setToAdd(result);
        return spatialResultInfo;

    }

    /**
     * Returns the map layer config details from system.config_map_layer table.
     *
     * @param languageCode The language code to use for localization of display
     * values.
     */
    @Override
    public List<ConfigMapLayer> getConfigMapLayerList(String languageCode) {
        Map params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_LANGUAGE_CODE, languageCode);
        params.put(CommonSqlProvider.PARAM_ORDER_BY_PART, ConfigMapLayer.QUERY_ORDER_BY);
        return getRepository().getEntityList(ConfigMapLayer.class, params);
    }

    /**
     * Returns the list of Crs
     *
     * @return
     */
    @Override
    public List<Crs> getCrsList() {
        Map params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_ORDER_BY_PART, Crs.ORDER_COLUMN);
        return getRepository().getEntityList(Crs.class, params);
    }

    /**
     * Executes a group of dynamic spatial queries using a filtering geometry.
     * Primarily used to obtain results for the Object Information Tool. Each
     * dynamic query must have a set of query fields configured in the
     * system.query_field table.
     *
     * @param queriesForSelection The list of dynamic spatial queries to execute
     * using the filtering geometry as a parameter.
     * @see #getGenericResultList(java.lang.String, java.util.Map)
     * getGenericResultList
     */
    @Override
    public List<ResultForSelectionInfo> getSpatialResultFromSelection(
            List<QueryForSelect> queriesForSelection) {
        List<ResultForSelectionInfo> results = new ArrayList<ResultForSelectionInfo>();
        for (QueryForSelect queryInfo : queriesForSelection) {
            Map params = new HashMap<String, Object>();
            params.put(ResultForSelectionInfo.PARAM_GEOMETRY, queryInfo.getFilteringGeometry());
            params.put(ResultForSelectionInfo.PARAM_SRID, queryInfo.getSrid());
            if (queryInfo.getLocale() != null) {
                params.put(CommonSqlProvider.PARAM_LANGUAGE_CODE, queryInfo.getLocale());
            } else {
                params.put(CommonSqlProvider.PARAM_LANGUAGE_CODE, "en");
            }
            ResultForSelectionInfo resultInfo = new ResultForSelectionInfo();
            resultInfo.setId(queryInfo.getId());
            resultInfo.setResult(this.getGenericResultList(queryInfo.getQueryName(), params));
            results.add(resultInfo);
        }
        return results;
    }

    /**
     * Retrieves the default map settings (i.e. default extent for the map and
     * srid) from the system.settings table.
     *
     * @see #getSettingList(java.lang.String) getSettingList
     */
    @Override
    public HashMap<String, String> getMapSettingList() {
        return this.getSettingList(Setting.QUERY_SQL_FOR_MAP_SETTINGS);
    }

    /**
     * Retrieves the system settings from the system.setting table using the
     * specified query.
     *
     * @param queryBody The query to use to obtain the settings from the
     * system.setting table.
     * @see #getMapSettingList() getMapSettingList
     */
    private HashMap<String, String> getSettingList(String queryBody) {
        HashMap settingMap = new HashMap();
        Map params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_QUERY, queryBody);
        List<Setting> settings = getRepository().getEntityList(Setting.class, params);
        if (settings != null && !settings.isEmpty()) {
            for (Setting setting : settings) {
                settingMap.put(setting.getId(), setting.getVl());
            }
        }
        return settingMap;
    }

    /**
     * Retrieves the history of changes and actions that have been applied to
     * the application.
     * <p>
     * Requires the {@linkplain RolesConstants#APPLICATION_VIEW_APPS} role.</p>
     *
     * @param applicationId The application to retrieve the log for
     */
    @Override
    @RolesAllowed(RolesConstants.APPLICATION_VIEW_APPS)
    public List<ApplicationLogResult> getApplicationLog(String applicationId) {
        Map params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_QUERY, SearchSqlProvider.buildApplicationLogSql());
        params.put(SearchSqlProvider.PARAM_APPLICATION_ID, applicationId);
        return getRepository().getEntityList(ApplicationLogResult.class, params);

    }

    /**
     * Executes a search across all Business Rules. Partial matches of the br
     * display name are supported.
     * <p>
     * Requires the {@linkplain RolesConstants#ADMIN_MANAGE_BR} role.</p>
     *
     * @param searchParams The parameters to use for the search.
     * @param lang The language code to use for localization of display values
     */
    @RolesAllowed(RolesConstants.ADMIN_MANAGE_BR)
    @Override
    public List<BrSearchResult> searchBr(BrSearchParams searchParams, String lang) {
        Map params = new HashMap<String, Object>();

        if (searchParams.getDisplayName() == null) {
            searchParams.setDisplayName("");
        }
        if (searchParams.getTargetCode() == null) {
            searchParams.setTargetCode("");
        }
        if (searchParams.getTechnicalTypeCode() == null) {
            searchParams.setTechnicalTypeCode("");
        }

        searchParams.setDisplayName(searchParams.getDisplayName().trim());

        params.put(CommonSqlProvider.PARAM_QUERY, BrSearchResult.SELECT_QUERY);
        params.put("lang", lang);
        params.put("displayName", searchParams.getDisplayName());
        params.put("technicalTypeCode", searchParams.getTechnicalTypeCode());
        params.put("targetCode", searchParams.getTargetCode());
        return getRepository().getEntityList(BrSearchResult.class, params);
    }

    /**
     * Retrieves all dynamic queries from the system.query table.
     */
    @Override
    public List<DynamicQuery> getQueryListAll() {
        return this.getRepository().getEntityList(DynamicQuery.class);
    }

    /**
     * Executes a search across all BA Units. Partial, case insensitive matches
     * of the name first part, name last part and owner name are supported.
     *
     * <p>
     * Requires the {@linkplain RolesConstants#ADMINISTRATIVE_BA_UNIT_SEARCH}
     * role.</p>
     *
     * @param searchParams The search criteria to use.
     * @return A maximum of 100 BA Units matching the search criteria.
     */
    @Override
    @RolesAllowed({RolesConstants.ADMINISTRATIVE_BA_UNIT_SEARCH, RolesConstants.APPLICATION_EDIT_APPS,
        RolesConstants.APPLICATION_CREATE_APPS})
    public List<BaUnitSearchResult> searchBaUnits(BaUnitSearchParams searchParams) {
        Map params = new HashMap<String, Object>();

        if (searchParams.getNameFirstPart() != null
                && searchParams.getNameFirstPart().trim().isEmpty()) {
            searchParams.setNameFirstPart(null);
        }
        if (searchParams.getNameLastPart() != null
                && searchParams.getNameLastPart().trim().isEmpty()) {
            searchParams.setNameLastPart(null);
        }
        if (searchParams.getOwnerName() != null && searchParams.getOwnerName().trim().isEmpty()) {
            searchParams.setOwnerName(null);
        }

        params.put(CommonSqlProvider.PARAM_QUERY,
                SearchSqlProvider.buildSearchBaUnitSql(searchParams.getNameFirstPart(),
                        searchParams.getNameLastPart(), searchParams.getOwnerName()));
        params.put(BaUnitSearchResult.QUERY_PARAM_OWNER_NAME, searchParams.getOwnerName());
        params.put(BaUnitSearchResult.QUERY_PARAM_NAME_FIRSTPART, searchParams.getNameFirstPart());
        params.put(BaUnitSearchResult.QUERY_PARAM_NAME_LASTPART, searchParams.getNameLastPart());
        return getRepository().getEntityList(BaUnitSearchResult.class, params);
    }

    /**
     * Retrieves the list of active spatial search options from the
     * system.map_search_option table.
     *
     * @param languageCode the language code of the client application.
     */
    @Override
    public List<SpatialSearchOption> getSpatialSearchOptions(String languageCode) {
        Map params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_LANGUAGE_CODE, languageCode);
        params.put(CommonSqlProvider.PARAM_WHERE_PART, SpatialSearchOption.QUERY_WHERE_ACTIVE);
        params.put(CommonSqlProvider.PARAM_ORDER_BY_PART, SpatialSearchOption.QUERY_ORDER_BY);
        return getRepository().getEntityList(SpatialSearchOption.class, params);
    }

    /**
     * Executes a search for spatial objects using the specified dynamic query.
     *
     * @param queryName The name of the dynamic query to use for the search
     * @param searchString The search string to use
     */
    @Override
    public List<SpatialSearchResult> searchSpatialObjects(String queryName,
            String searchString, int srid) {
        Map params = new HashMap<String, Object>();
        params.put(SpatialSearchResult.PARAM_SEARCH_STRING, searchString);
        params.put(SpatialSearchResult.PARAM_SRID, srid);
        return executeDynamicQuery(SpatialSearchResult.class, queryName, params);
    }

    /**
     * Returns list of rights for export by given parameters.
     */
    @Override
    @RolesAllowed(RolesConstants.ADMINISTRATIVE_RIGHTS_EXPORT)
    public List<RightsExportResult> searchRightsForExport(RightsExportParams searchParams) {
        Map params = new HashMap<String, Object>();
        Calendar cal = Calendar.getInstance();

        if (searchParams.getDateFrom() == null) {
            searchParams.setDateFrom(new GregorianCalendar(1, 1, 1, 0, 0).getTime());
        } else {
            cal.setTime(searchParams.getDateFrom());
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            searchParams.setDateFrom(cal.getTime());
        }

        if (searchParams.getDateTo() == null) {
            searchParams.setDateTo(new GregorianCalendar(2500, 1, 1, 23, 59).getTime());
        } else {
            cal.setTime(searchParams.getDateTo());
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            searchParams.setDateTo(cal.getTime());
        }

        if (searchParams.getRightTypeCode() == null) {
            searchParams.setRightTypeCode("");
        }
        params.put(CommonSqlProvider.PARAM_QUERY, RightsExportResult.SEARCH_QUERY);
        params.put(RightsExportResult.PARAM_DATE_FROM, searchParams.getDateFrom());
        params.put(RightsExportResult.PARAM_DATE_TO, searchParams.getDateTo());
        params.put(RightsExportResult.PARAM_RIGHT_TYPE, searchParams.getRightTypeCode());
        return getRepository().getEntityList(RightsExportResult.class, params);
    }

    /**
     * Get the extent of the public display map.
     *
     * @param nameLastPart
     * @return
     */
    @Override
    public byte[] getExtentOfPublicDisplayMap(String nameLastPart) {
        String sqlToGetExtent = "select st_asewkb(st_extent(co.geom_polygon)) as extent "
                + " from cadastre.cadastre_object co where type_code= 'parcel' "
                + " and status_code= 'current' and name_lastpart =#{name_lastpart}";
        String paramLastPart = "name_lastpart";
        Map params = new HashMap();
        params.put(CommonSqlProvider.PARAM_QUERY, sqlToGetExtent);
        params.put(paramLastPart, nameLastPart);
        List result = getRepository().executeSql(params);
        byte[] value = null;
        if (result != null && result.size() > 0 && result.get(0) != null) {
            value = (byte[]) ((HashMap) result.get(0)).get("extent");
        }
        return value;
    }

    @Override
    public String getMapCenterLabel(byte[] mapCenterPoint) {
        String sql = "select cadastre.get_map_center_label(#{map_center_point}) as vl";
        String paramMapCenterPoint = "map_center_point";
        Map params = new HashMap();
        params.put(CommonSqlProvider.PARAM_QUERY, sql);
        params.put(paramMapCenterPoint, mapCenterPoint);
        List result = getRepository().executeSql(params);
        String value = "";
        if (result != null && result.size() > 0 && result.get(0) != null) {
            value = ((HashMap) result.get(0)).get("vl").toString();
        }
        return value;
    }

    /**
     * Returns list of {@link ClaimSpatialSearchResult} objects by provided
     * bounding box.
     *
     * @param searchParams Search parameters
     * @return
     */
    @Override
    //@RolesAllowed({RolesConstants.CS_ACCESS_CS})
    public List<ClaimSpatialSearchResult> getClaimsByBox(ClaimSpatialSearchParams searchParams) {
        Map params = new HashMap();
        params.put(CommonSqlProvider.PARAM_WHERE_PART, ClaimSpatialSearchResult.WHERE_SEARCH_BY_BOX);
        params.put(CommonSqlProvider.PARAM_LIMIT_PART, searchParams.getLimit());
        params.put(ClaimSearchResult.PARAM_RECORDER, getUserName());
        params.put(ClaimSpatialSearchResult.PARAM_ENVELOPE,
                String.format(ClaimSpatialSearchResult.ENVELOPE,
                        searchParams.getMinX(), searchParams.getMinY(),
                        searchParams.getMaxX(), searchParams.getMaxY()));
        return getRepository().getEntityList(ClaimSpatialSearchResult.class, params);
    }

    /**
     * Returns list of {@link ClaimSpatialSearchResult} representing all claims.
     *
     * @return
     */
    @Override
    @RolesAllowed({RolesConstants.CS_ACCESS_CS, RolesConstants.INVESTOR})
    public List<ClaimSpatialSearchResult> getAllClaims() {
        Map params = new HashMap();
        params.put(CommonSqlProvider.PARAM_WHERE_PART, ClaimSpatialSearchResult.WHERE_SEARCH_ALL);
        params.put(ClaimSearchResult.PARAM_RECORDER, getUserName());
        return getRepository().getEntityList(ClaimSpatialSearchResult.class, params);
    }

    /**
     * Returns {@link ClaimSearchResult} by x and y coordinates.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param langCode Language code
     * @return
     */
    @Override
    //@RolesAllowed({RolesConstants.CS_ACCESS_CS})
    public ClaimSearchResult getClaimByCoordinates(String x, String y, String langCode) {
        if (StringUtility.isEmpty(x) || StringUtility.isEmpty(y)) {
            return null;
        }

        String point = "POINT(" + x + " " + y + ")";
        String userBoundary = "";
        String statusCode = "";
        
        UserSearchResult user = searchCurrentUser();
        if(user != null) {
            userBoundary = StringUtility.empty(user.getAdminBoundaryId());
        }
        if(isInRole(RolesConstants.INVESTOR)) {
            statusCode = ClaimStatusConstants.MODERATED;
        }
        
        HashMap params = new HashMap();
        params.put(CommonSqlProvider.PARAM_QUERY, ClaimSearchResult.QUERY_SEARCH_BY_POINT);
        params.put(ClaimSearchResult.PARAM_POINT, point);
        params.put(CommonSqlProvider.PARAM_LANGUAGE_CODE, langCode);
        params.put(ClaimSearchResult.PARAM_USER_BOUNDARY_ID, userBoundary);
        params.put(ClaimSearchResult.PARAM_STATUS_CODE, statusCode);
        
        List<ClaimSearchResult> list = getRepository().getEntityList(ClaimSearchResult.class, params);
        if (list == null || list.size() < 1) {
            return null;
        } else {
            return list.get(0);
        }
    }

    public UserSearchResult searchCurrentUser() {
        Map params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_QUERY, UserSearchResult.QUERY_BY_USER_NAME);
        params.put(UserSearchResult.PARAM_USERNAME, this.getUserName());
        return getRepository().getEntity(UserSearchResult.class, params);
    }
    
    /**
     * Searched and returns list of {@link ClaimSearchResult}.
     *
     * @param searchParams Search parameters
     * @return
     */
    @Override
    @RolesAllowed({RolesConstants.CS_ACCESS_CS, RolesConstants.INVESTOR})
    public List<ClaimSearchResult> searchClaims(ClaimSearchParams searchParams) {
        Map params = new HashMap<String, Object>();
        
        UserSearchResult user = searchCurrentUser();
        if(user != null) {
            searchParams.setUserBoundaryId(user.getAdminBoundaryId());
        }
            
        // prepare params
        if (searchParams.getLodgementDateFrom() != null || searchParams.getLodgementDateTo() != null) {
            searchParams.setLodgementDateFrom(DateUtility.minimizeDate(searchParams.getLodgementDateFrom()));
            searchParams.setLodgementDateTo(DateUtility.maximizeDate(searchParams.getLodgementDateTo()));
        }
        
        searchParams.setDescription(StringUtility.empty(searchParams.getDescription()));
        searchParams.setClaimNumber(StringUtility.empty(searchParams.getClaimNumber()));
        searchParams.setClaimantName(StringUtility.empty(searchParams.getClaimantName()));
        searchParams.setStatusCode(StringUtility.empty(searchParams.getStatusCode()));
        searchParams.setLanguageCode(StringUtility.empty(searchParams.getLanguageCode()));
        searchParams.setUserBoundaryId(StringUtility.empty(searchParams.getUserBoundaryId()));
        searchParams.setBoundaryId(StringUtility.empty(searchParams.getBoundaryId()));

        // Limit investors for approvaed only
        if(isInRole(RolesConstants.INVESTOR)) {
            searchParams.setStatusCode(ClaimStatusConstants.MODERATED);
        }
        
        // put params into map
        params.put(CommonSqlProvider.PARAM_QUERY, ClaimSearchResult.QUERY_SEARCH);
        params.put(CommonSqlProvider.PARAM_LANGUAGE_CODE, searchParams.getLanguageCode());
        params.put(ClaimSearchResult.PARAM_DATE_FROM, searchParams.getLodgementDateFrom());
        params.put(ClaimSearchResult.PARAM_DATE_TO, searchParams.getLodgementDateTo());
        params.put(ClaimSearchResult.PARAM_DESCRIPTION, searchParams.getDescription());
        params.put(ClaimSearchResult.PARAM_CLAIM_NUMBER, searchParams.getClaimNumber());
        params.put(ClaimSearchResult.PARAM_NAME, searchParams.getClaimantName());
        params.put(ClaimSearchResult.PARAM_SEARCH_BY_USER, searchParams.isSearchByUser());
        params.put(ClaimSearchResult.PARAM_RECORDER, getUserName());
        params.put(ClaimSearchResult.PARAM_STATUS_CODE, searchParams.getStatusCode());
        params.put(ClaimSearchResult.PARAM_USER_BOUNDARY_ID, searchParams.getUserBoundaryId());
        params.put(ClaimSearchResult.PARAM_BOUNDARY_ID, searchParams.getBoundaryId());

        return getRepository().getEntityList(ClaimSearchResult.class, params);
    }

    @Override
    @RolesAllowed({RolesConstants.CS_ACCESS_CS, RolesConstants.INVESTOR})
    public List<MapSearchResult> searchMap(String searchString) {
        Map params = new HashMap<String, Object>();
        String name = "";
        String point = "";
        String claimNumber = "";
        String userBoundary = "";
        String statusCode = "";
        
        // prepare params
        if (!StringUtility.isEmpty(searchString)) {
            searchString = searchString.trim().replace("#", "");
            // Try to guess parameters
            String numbers[] = searchString.replace(" ", "").split(",");
            if (numbers != null && numbers.length > 1) {
                if (NumberUtils.isNumber(numbers[0]) && NumberUtils.isNumber(numbers[1])) {
                    point = String.format("POINT(%s %s)", numbers[0], numbers[1]);
                }
            } else {
                name = searchString;
                claimNumber = searchString;
            }
        }
        
        UserSearchResult user = searchCurrentUser();
        if(user != null) {
            userBoundary = StringUtility.empty(user.getAdminBoundaryId());
        }
        if(isInRole(RolesConstants.INVESTOR)) {
            statusCode = ClaimStatusConstants.MODERATED;
        }

        params.put(CommonSqlProvider.PARAM_QUERY, MapSearchResult.QUERY_SEARCH);
        params.put(MapSearchResult.PARAM_NAME, name);
        params.put(MapSearchResult.PARAM_STATUS_CODE, statusCode);
        params.put(MapSearchResult.PARAM_USER_BOUNDARY_ID, userBoundary);
        params.put(MapSearchResult.PARAM_CLAIM_NUMBER, claimNumber);
        params.put(MapSearchResult.PARAM_POINT, point);

        return getRepository().getEntityList(MapSearchResult.class, params);
    }

    @Override
    public List<SpatialResult> getPlanCadastreObjects(String cadastreObjectId) {
        Map params = new HashMap<String, Object>();
        params.put(SpatialResult.PARAM_CADASTRE_OBJECT_ID, cadastreObjectId);
        params.put(CommonSqlProvider.PARAM_QUERY, SpatialResult.QUERY_GET_PLAN_CADASTRE_OBJECTS);
        return getRepository().getEntityList(SpatialResult.class, params);
    }

    @Override
    public byte[] transform(byte[] wkbGeom, int srid) {
        Map params = new HashMap();
        params.put(
                CommonSqlProvider.PARAM_QUERY,
                "select st_asewkb(st_transform(#{geom}, #{srid}))");
        params.put("srid", srid);
        params.put("geom", wkbGeom);
        return getRepository().getScalar(byte[].class, params);
    }

    @Override
    public List<ClaimSearchResult> searchAssignedClaims(String langCode) {
        Map params = new HashMap<String, Object>();

        params.put(CommonSqlProvider.PARAM_QUERY, ClaimSearchResult.QUERY_SEARCH_ASSIGNED_TO_USER);
        params.put(CommonSqlProvider.PARAM_LANGUAGE_CODE, langCode);
        params.put(ClaimSearchResult.PARAM_USERNAME, getUserName());
        return getRepository().getEntityList(ClaimSearchResult.class, params);
    }

    @Override
    @RolesAllowed({RolesConstants.CS_ACCESS_CS})
    public List<ClaimSearchResult> searchClaimsForReview(String langCode, boolean includeAssigned) {
        Map params = new HashMap<String, Object>();

        params.put(CommonSqlProvider.PARAM_LANGUAGE_CODE, langCode);

        if (includeAssigned) {
            params.put(CommonSqlProvider.PARAM_QUERY, ClaimSearchResult.QUERY_SEARCH_FOR_REVIEW_ALL);
        } else {
            params.put(CommonSqlProvider.PARAM_QUERY, ClaimSearchResult.QUERY_SEARCH_FOR_REVIEW);
        }

        return getRepository().getEntityList(ClaimSearchResult.class, params);
    }

    @Override
    @RolesAllowed({RolesConstants.CS_ACCESS_CS})
    public List<ClaimSearchResult> searchClaimsForModeration(String langCode, boolean includeAssigned) {
        Map params = new HashMap<String, Object>();

        params.put(CommonSqlProvider.PARAM_LANGUAGE_CODE, langCode);

        if (includeAssigned) {
            params.put(CommonSqlProvider.PARAM_QUERY, ClaimSearchResult.QUERY_SEARCH_FOR_MODERATION_ALL);
        } else {
            params.put(CommonSqlProvider.PARAM_QUERY, ClaimSearchResult.QUERY_SEARCH_FOR_MODERATION);
        }

        return getRepository().getEntityList(ClaimSearchResult.class, params);
    }

    @Override
    @RolesAllowed({RolesConstants.CS_ACCESS_CS})
    public List<AdministrativeBoundarySearchResult> searchAllAdministrativeBoundaries(String langCode) {
        Map params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_LANGUAGE_CODE, langCode);
        params.put(CommonSqlProvider.PARAM_QUERY, AdministrativeBoundarySearchResult.QUERY_GET_ALL);
        return getRepository().getEntityList(AdministrativeBoundarySearchResult.class, params);
    }
    
    @Override
    @RolesAllowed({RolesConstants.CS_ACCESS_CS})
    public List<AdministrativeBoundarySearchResult> searchApprovedAdministrativeBoundaries(String langCode) {
        Map params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_LANGUAGE_CODE, langCode);
        params.put(CommonSqlProvider.PARAM_QUERY, AdministrativeBoundarySearchResult.QUERY_GET_APPROVED);
        return getRepository().getEntityList(AdministrativeBoundarySearchResult.class, params);
    }

    @Override
    @RolesAllowed({RolesConstants.CS_ACCESS_CS})
    public List<AdministrativeBoundarySearchResult> searchParentAdministrativeBoundaries(String langCode) {
        Map params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_LANGUAGE_CODE, langCode);
        params.put(CommonSqlProvider.PARAM_QUERY, AdministrativeBoundarySearchResult.QUERY_GET_ALL_PARENTS);
        return getRepository().getEntityList(AdministrativeBoundarySearchResult.class, params);
    }

    @Override
    @RolesAllowed({RolesConstants.CS_ACCESS_CS})
    public List<AdministrativeBoundarySearchResult> searchChildAdministrativeBoundaries(String parentId, String langCode) {
        Map params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_LANGUAGE_CODE, langCode);
        params.put(AdministrativeBoundarySearchResult.PARAM_PARENT_ID, parentId);
        params.put(CommonSqlProvider.PARAM_QUERY, AdministrativeBoundarySearchResult.QUERY_GET_CHILDREN);
        return getRepository().getEntityList(AdministrativeBoundarySearchResult.class, params);
    }

    @Override
    @RolesAllowed({RolesConstants.CS_ACCESS_CS})
    public List<AdministrativeBoundarySearchResult> searchParentAdministrativeBoundaries(String id, String langCode) {
        Map params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_LANGUAGE_CODE, langCode);
        params.put(AdministrativeBoundarySearchResult.PARAM_ID, id);
        params.put(CommonSqlProvider.PARAM_QUERY, AdministrativeBoundarySearchResult.QUERY_GET_PARENTS);
        return getRepository().getEntityList(AdministrativeBoundarySearchResult.class, params);
    }

    @Override
    public String getFullLocation(String boundaryId, String langCode) {
        List<AdministrativeBoundarySearchResult> boundaries = searchParentAdministrativeBoundaries(boundaryId, langCode);
        if (boundaries == null || boundaries.size() < 1) {
            return "";
        }
        String location = "";
        for (AdministrativeBoundarySearchResult boundary : boundaries) {
            if (location.equals("")) {
                location = boundary.getName();
            } else {
                location = location + ", " + boundary.getName();
            }
        }
        return location;
    }

    /**
     * Returns {@link AdministrativeBoundaryWithGeomSearchResult} by x and y
     * coordinates.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param langCode Language code
     * @return
     */
    //@RolesAllowed({RolesConstants.CS_ACCESS_CS})
    @Override
    public AdministrativeBoundaryWithGeomSearchResult getAdministrativeBoundaryByCoordinates(String x, String y, String langCode) {
        if (StringUtility.isEmpty(x) || StringUtility.isEmpty(y)) {
            return null;
        }

        String point = "POINT(" + x + " " + y + ")";
        HashMap params = new HashMap();
        params.put(CommonSqlProvider.PARAM_QUERY, AdministrativeBoundaryWithGeomSearchResult.QUERY_SEARCH_BY_POINT);
        params.put(AdministrativeBoundaryWithGeomSearchResult.PARAM_POINT, point);
        params.put(CommonSqlProvider.PARAM_LANGUAGE_CODE, langCode);
        List<AdministrativeBoundaryWithGeomSearchResult> list = getRepository().getEntityList(AdministrativeBoundaryWithGeomSearchResult.class, params);
        if (list == null || list.size() < 1) {
            return null;
        } else {
            return list.get(0);
        }
    }
    /**
     * Returns {@link SpatialUnitWithGeomSearchResult} by x and y
     * coordinates.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param langCode Language code
     * @return
     */
    //@RolesAllowed({RolesConstants.CS_ACCESS_CS})
    @Override
    public List<SpatialUnitWithGeomSearchResult> getSpatialUnitsByCoordinates(String x, String y, String langCode) {
        if (StringUtility.isEmpty(x) || StringUtility.isEmpty(y)) {
            return null;
        }

        String point = "POINT(" + x + " " + y + ")";
        HashMap params = new HashMap();
        params.put(CommonSqlProvider.PARAM_QUERY, SpatialUnitWithGeomSearchResult.QUERY_SEARCH_BY_POINT);
        params.put(SpatialUnitWithGeomSearchResult.PARAM_POINT, point);
        params.put(CommonSqlProvider.PARAM_LANGUAGE_CODE, langCode);
        
        return getRepository().getEntityList(SpatialUnitWithGeomSearchResult.class, params);
    }

    //@RolesAllowed({RolesConstants.CS_ACCESS_CS})
    @Override
    public List<GeoJsonClaim> getGeoJsonClaimsByBoundary(String boundaryId) {
        HashMap params = new HashMap();
        if (StringUtility.isEmpty(boundaryId)) {
            params.put(CommonSqlProvider.PARAM_QUERY, GeoJsonClaim.QUERY_SEARCH_ALL);
        } else {
            params.put(CommonSqlProvider.PARAM_QUERY, GeoJsonClaim.QUERY_SEARCH_BY_BOUNDARY);
            params.put(GeoJsonClaim.PARAM_BOUNDARY_ID, boundaryId);
        }
        return getRepository().getEntityList(GeoJsonClaim.class, params);
    }

    //@RolesAllowed({RolesConstants.CS_ACCESS_CS})
    @Override
    public GeoJsonAdministrativeBoundary getGeoJsonAdministrativeBoundary(String id) {
        HashMap params = new HashMap();
        params.put(CommonSqlProvider.PARAM_QUERY, GeoJsonAdministrativeBoundary.QUERY_SEARCH_BY_ID);
        params.put(GeoJsonAdministrativeBoundary.PARAM_ID, id);
        return getRepository().getEntity(GeoJsonAdministrativeBoundary.class, params);
    }
    
    @RolesAllowed({RolesConstants.CS_VIEW_REPORTS})
    @Override
    public List<PublicDisplaySearchResult> searchClaimsForPublicDisplay(String langCode, String boundaryId){
        Map params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_LANGUAGE_CODE, langCode);
        params.put(PublicDisplaySearchResult.PARAM_BOUNDARY_ID, StringUtility.empty(boundaryId));
        params.put(CommonSqlProvider.PARAM_QUERY, PublicDisplaySearchResult.QUERY_SEARCH_BY_BOUNDARY);
        return getRepository().getEntityList(PublicDisplaySearchResult.class, params);
    }
}
