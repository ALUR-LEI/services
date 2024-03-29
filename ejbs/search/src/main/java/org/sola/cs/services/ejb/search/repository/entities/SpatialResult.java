/**
 * ******************************************************************************************
 * Copyright (C) 2014 - Food and Agriculture Organization of the United Nations (FAO).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice,this list
 *       of conditions and the following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright notice,this list
 *       of conditions and the following disclaimer in the documentation and/or other
 *       materials provided with the distribution.
 *    3. Neither the name of FAO nor the names of its contributors may be used to endorse or
 *       promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,STRICT LIABILITY,OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * *********************************************************************************************
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sola.cs.services.ejb.search.repository.entities;

import javax.persistence.Column;
import javax.persistence.Id;
import org.sola.services.common.repository.entities.AbstractReadOnlyEntity;

/**
 *
 * @author soladev
 */
public class SpatialResult extends AbstractReadOnlyEntity {
    
    public final static String PARAM_CADASTRE_OBJECT_ID = "cadastre_object_id";
    public final static String QUERY_GET_PLAN_CADASTRE_OBJECTS = 
       "select adject.id, adject.name_firstpart as label, st_asewkb(adject.geom_polygon) as the_geom, "
            + "cast(adject.id = #{" + PARAM_CADASTRE_OBJECT_ID + "} as varchar) as filter_category\n" +
       "from cadastre.cadastre_object main, cadastre.cadastre_object adject\n" +
       "where main.id= #{" + PARAM_CADASTRE_OBJECT_ID + "} and st_dwithin(main.geom_polygon, adject.geom_polygon, 0.1)"
            + " order by 4 desc, st_area(adject.geom_polygon) asc";

    @Id
    @Column(name = "id")
    private String id; 
    @Column(name = "label")
    private String label;
    @Column(name = "the_geom")
    private byte[] theGeom;
    @Column (name = "filter_category")
    private String filterCategory;
    
    public SpatialResult() {
        super();
    }

    public String getFilterCategory() {
        return filterCategory;
    }

    public void setFilterCategory(String filterCategory) {
        this.filterCategory = filterCategory;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public byte[] getTheGeom() {
        return theGeom;
    }

    public void setTheGeom(byte[] theGeom) { //NOSONAR
        this.theGeom = theGeom; //NOSONAR
    }
       
}
