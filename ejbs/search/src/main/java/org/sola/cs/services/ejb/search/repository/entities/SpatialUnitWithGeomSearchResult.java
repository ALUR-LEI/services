package org.sola.cs.services.ejb.search.repository.entities;

import javax.persistence.Column;
import org.sola.services.common.repository.CommonSqlProvider;
import org.sola.services.common.repository.entities.AbstractReadOnlyEntity;

public class SpatialUnitWithGeomSearchResult extends AbstractReadOnlyEntity {

    @Column
    private String id;
    @Column
    private String geom;
    @Column (name = "label")
    private String label;
    @Column (name = "level_id")
    private String levelId;
    @Column(name = "level_name")
    private String levelName;
    
    
    public static final String PARAM_POINT = "point";

    public static final String QUERY_SEARCH_BY_POINT
            = "SELECT su.id, st_astext(su.geom) as geom, su.label, su.level_id, lv.name as level_name \n"
            + "FROM (cadastre.spatial_unit su  \n"
            + "INNER JOIN cadastre.level lv ON su.level_id = lv.id \n"
            + ") \n"
            + "WHERE ST_Contains(su.geom, ST_GeomFromText(#{" + PARAM_POINT + "}, St_SRID(su.geom))) ";

    public SpatialUnitWithGeomSearchResult() {
        super();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGeom() {
        return geom;
    }

    public void setGeom(String geom) {
        this.geom = geom;
    }
   
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
   
    public String getLevelId() {
        return levelId;
    }

    public void setLevelId(String levelId) {
        this.levelId = levelId;
    }

    public String getLevelName() {
        return levelName;
    }

    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }
}
