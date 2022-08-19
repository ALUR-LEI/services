package org.sola.cs.services.ejbs.communityboundary.entities;

import javax.persistence.Column;
import org.sola.services.common.repository.entities.AbstractReadOnlyEntity;

/**
 * Spatial representation of the claim
 */
public class AdministrativeBoundarySpatial extends AbstractReadOnlyEntity {

    public static final String PARAM_CUSTOM_SRID = "customSrid";
    public static final String PARAM_BOUNDARY_ID = "boundaryId";
    public static final String QUERY_GET_BY_ID = "WITH target_geom AS ("
            + "  select st_astext(st_buffer(geom, 0.0001)) as geom "
            + "  from opentenure.administrative_boundary where id =#{ " + PARAM_BOUNDARY_ID + "} and geom is not null "
            + ") "
            + "select id, nr, status_code, "
            + "st_astext(case when coalesce(#{ " + PARAM_CUSTOM_SRID + "},0) = 0 then geom else st_transform(st_setsrid(geom,4326),#{ " + PARAM_CUSTOM_SRID + "}) end) as geom, "
            + "(case when id =#{ " + PARAM_BOUNDARY_ID + "} then true else false end) as target "
            + "from opentenure.administrative_boundary "
            + "where geom is not null and ST_Intersects(geom,(select geom from target_geom)::geometry)";

    @Column
    private String id;
    @Column
    private String name;
    @Column(name = "status_code")
    private String statusCode;
    @Column
    private boolean target;
    @Column
    private String geom;

    public AdministrativeBoundarySpatial() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String nr) {
        this.name = name;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public boolean isTarget() {
        return target;
    }

    public void setTarget(boolean target) {
        this.target = target;
    }

    public String getGeom() {
        return geom;
    }

    public void setGeom(String geom) {
        this.geom = geom;
    }
}
