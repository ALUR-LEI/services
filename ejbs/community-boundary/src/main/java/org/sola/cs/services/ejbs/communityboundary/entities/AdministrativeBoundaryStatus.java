package org.sola.cs.services.ejbs.communityboundary.entities;

import javax.persistence.Table;
import org.sola.services.common.repository.DefaultSorter;
import org.sola.services.common.repository.entities.AbstractCodeEntity;

@Table(name = "administrative_boundary_status", schema = "opentenure")
@DefaultSorter(sortString="display_value")
public class AdministrativeBoundaryStatus extends AbstractCodeEntity {
    public AdministrativeBoundaryStatus(){
        super();
    }
}
