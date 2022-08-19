package org.sola.cs.services.ejb.search.repository.entities;

import javax.persistence.Column;
import org.sola.services.common.repository.CommonSqlProvider;
import org.sola.services.common.repository.entities.AbstractReadOnlyEntity;

public class SpatialUnitSearchResult extends AbstractReadOnlyEntity {

    @Column
    private String id;
    @Column(name = "dimension_code")
    private String dimensionCode;
    @Column(name = "dimension_name")
    private String dimensionName;
    @Column(name = "label")
    private String label;
    @Column(name = "surface_relation_code")
    private String surfaceRelationCode;
    @Column(name = "level_id")
    private String levelId;
    @Column(name = "level_name")
    private String levelName;

    public SpatialUnitSearchResult() {
        super();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDimensionCode() {
        return dimensionCode;
    }

    public void setDimensionCode(String dimensionCode) {
        this.dimensionCode = dimensionCode;
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
