/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sis.metadata.iso.citation;

import java.net.URI;
import org.opengis.metadata.citation.Role;
import org.opengis.metadata.citation.Citation;
import org.opengis.metadata.citation.OnLineFunction;
import org.opengis.metadata.citation.PresentationForm;
import org.apache.sis.metadata.iso.DefaultIdentifier;
import org.apache.sis.util.iso.SimpleInternationalString;
import org.apache.sis.internal.util.Constants;
import org.apache.sis.util.Static;

import static java.util.Collections.singleton;


/**
 * Hard-coded citation constants used for testing purpose only.
 * We use those hard-coded constants instead than the ones defined in the
 * {@link org.apache.sis.metadata.iso.citation.Citations} class in order
 * to protect the test suite against any change in the definition of the
 * above-cited public constants.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @since   0.3
 * @version 0.5
 * @module
 */
public final strictfp class HardCodedCitations extends Static {
    /**
     * The <a href="http://www.opengeospatial.org">Open Geospatial consortium</a> organization.
     * "Open Geospatial consortium" is the new name for "OpenGIS consortium".
     * An {@linkplain Citation#getAlternateTitles() alternate title} for this citation is "OGC"
     * (according ISO 19115, alternate titles often contain abbreviations).
     */
    public static final DefaultCitation OGC;
    static {
        final DefaultCitation c = new DefaultCitation("Open Geospatial consortium");
        c.setAlternateTitles(singleton(new SimpleInternationalString("OGC")));
        c.setPresentationForms(singleton(PresentationForm.DOCUMENT_DIGITAL));
        c.setIdentifiers(singleton(new DefaultIdentifier(Constants.OGC)));
        c.freeze();
        OGC = c;
    }

    /**
     * The <a href="http://www.iso.org/">International Organization for Standardization</a>
     * organization. An {@linkplain Citation#getAlternateTitles() alternate title} for this
     * citation is "ISO" (according ISO 19115, alternate titles often contain abbreviations).
     */
    public static final DefaultCitation ISO;
    static {
        final DefaultCitation c = new DefaultCitation("International Organization for Standardization");
        c.setAlternateTitles(singleton(new SimpleInternationalString("ISO")));
        c.setPresentationForms(singleton(PresentationForm.DOCUMENT_DIGITAL));
        c.setIdentifiers(singleton(new DefaultIdentifier("ISO")));
        c.freeze();
        ISO = c;
    }

    /**
     * The ISO 19115 standard.
     */
    public static final DefaultCitation ISO_19115;
    static {
        final DefaultCitation c = new DefaultCitation("ISO 19115");
        c.setPresentationForms(singleton(PresentationForm.DOCUMENT_DIGITAL));
        c.freeze();
        ISO_19115 = c;
    }

    /**
     * The <a href="http://www.iogp.org">International Association of Oil &amp; Gas Producers</a> organization.
     * This organization is responsible for maintainance of {@link #EPSG} database.
     * An {@linkplain Citation#getAlternateTitles() alternate title} for this citation is "IOGP"
     * (according ISO 19115, alternate titles often contain abbreviations).
     */
    public static final DefaultCitation IOGP;
    static {
        final DefaultCitation c = new DefaultCitation("International Association of Oil & Gas Producers");
        c.setAlternateTitles(singleton(new SimpleInternationalString("IOGP")));
        c.setIdentifiers(singleton(new DefaultIdentifier("IOGP")));
        c.freeze();
        IOGP = c;
    }

    /**
     * The <a href="http://www.epsg.org">European Petroleum Survey Group</a> authority.
     * An {@linkplain Citation#getAlternateTitles() alternate title} for this citation is "EPSG"
     * (according ISO 19115, alternate titles often contain abbreviations). In addition,
     * this citation contains the "EPSG" {@linkplain Citation#getIdentifiers() identifier}.
     *
     * <p>String representation:</p>
     *
     * {@preformat text
     *   Citation
     *     ├─Title………………………………………………………… European Petroleum Survey Group
     *     ├─Alternate title……………………………… EPSG
     *     ├─Identifier
     *     │   └─Code………………………………………………… EPSG
     *     ├─Cited responsible party
     *     │   ├─Party
     *     │   │   ├─Name……………………………………… International Association of Oil & Gas Producers
     *     │   │   └─Contact info
     *     │   │       └─Online resource
     *     │   │           ├─Linkage………… http://www.epsg.org
     *     │   │           └─Function……… Information
     *     │   └─Role………………………………………………… Principal investigator
     *     └─Presentation form………………………… Table digital
     * }
     */
    public static final DefaultCitation EPSG;
    static {
        final DefaultOnlineResource r = new DefaultOnlineResource(URI.create("http://www.epsg.org"));
        r.setFunction(OnLineFunction.INFORMATION);

        final DefaultResponsibility p = new DefaultResponsibility(Role.PRINCIPAL_INVESTIGATOR, null,
                new DefaultOrganisation(IOGP.getTitle(), null, null, new DefaultContact(r)));

        final DefaultCitation c = new DefaultCitation("European Petroleum Survey Group");
        c.setAlternateTitles(singleton(new SimpleInternationalString("EPSG")));
        c.setPresentationForms(singleton(PresentationForm.TABLE_DIGITAL));
        c.setIdentifiers(singleton(new DefaultIdentifier(Constants.EPSG)));
        c.setCitedResponsibleParties(singleton(p));
        c.freeze();
        EPSG = c;
    }

    /**
     * The <a href="http://www.remotesensing.org/geotiff/geotiff.html">GeoTIFF</a> specification.
     */
    public static final DefaultCitation GEOTIFF;
    static {
        final DefaultCitation c = new DefaultCitation("GeoTIFF");
        c.setPresentationForms(singleton(PresentationForm.DOCUMENT_DIGITAL));
        c.freeze();
        GEOTIFF = c;
    }

    /**
     * The <a href="http://sis.apache.org">Apache SIS</a> project.
     */
    public static final DefaultCitation SIS;
    static {
        final DefaultCitation c = new DefaultCitation(Constants.SIS);
        c.freeze();
        SIS = c;
    }

    /**
     * Do not allow instantiation of this class.
     */
    private HardCodedCitations() {
    }
}
