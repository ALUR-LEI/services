package org.sola.cs.services.ejbs.claim.entities;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import org.sola.services.common.repository.AccessFunctions;
import org.sola.services.common.repository.entities.AbstractVersionedEntity;

@Table(schema = "cadastre", name = "spatial_unit")
public class SpatialUnit extends AbstractVersionedEntity {

    @Id
    @Column
    private String id;
    @Column(name = "dimension_code")
    private String dimensionCode;
    @Column(name = "label")
    private String label;
    @Column(name = "surface_relation_code")
    private String surfaceRelationCode;
    @Column(name = "level_id")
    private String levelId;
    @Column(name = "land_use_code")
    private String landUseCode;
    @Column(name = "geom")
    @AccessFunctions(onSelect = "ST_AsText(geom)")
    private String geom;

 

    public SpatialUnit() {
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

    public String getSurfaceRelationCode() {
        return surfaceRelationCode;
    }

    public void setSurfaceRelationCode(String surfaceRelationCode) {
        this.surfaceRelationCode = surfaceRelationCode;
    }

    public String getLevelId() {
        return levelId;
    }

    public void setLevelId(String levelId) {
        this.levelId = levelId;
    }

    public String getLandUseCode() {
        return landUseCode;
    }

    public void setLandUseCode(String statusCode) {
        this.landUseCode = statusCode;
    }

    public String getGeom() {
        return geom;
    }

    public void setGeom(String geom) {
        this.geom = geom;
    }
}
