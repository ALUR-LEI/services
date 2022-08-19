package org.sola.cs.services.ejbs.claim.entities;

import javax.persistence.Table;
import org.sola.services.common.repository.DefaultSorter;
import org.sola.services.common.repository.entities.AbstractCodeEntity;

@Table(name = "land_use_type", schema = "cadastre")
@DefaultSorter(sortString="display_value")
public class SpatialUnitLandUse extends AbstractCodeEntity {
    public SpatialUnitLandUse(){
        super();
    }
}
