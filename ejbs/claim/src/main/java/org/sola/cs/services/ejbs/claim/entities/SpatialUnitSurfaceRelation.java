package org.sola.cs.services.ejbs.claim.entities;

import javax.persistence.Table;
import org.sola.services.common.repository.DefaultSorter;
import org.sola.services.common.repository.entities.AbstractCodeEntity;

@Table(name = "surface_relation_type", schema = "cadastre")
@DefaultSorter(sortString="display_value")
public class SpatialUnitSurfaceRelation extends AbstractCodeEntity {
    public SpatialUnitSurfaceRelation(){
        super();
    }
}
