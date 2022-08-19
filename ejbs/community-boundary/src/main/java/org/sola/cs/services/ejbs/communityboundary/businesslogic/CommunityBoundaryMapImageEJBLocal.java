package org.sola.cs.services.ejbs.communityboundary.businesslogic;

import java.awt.image.BufferedImage;
import javax.ejb.Local;
import org.sola.services.common.ejbs.AbstractEJBLocal;

@Local
public interface CommunityBoundaryMapImageEJBLocal extends AbstractEJBLocal {

    int getBestScaleForMapImage(String boundaryId, int width, int height);

    BufferedImage getBoundaryMapImage(String boundaryId, int width, int height, double scale, boolean drawScale, String scaleLabel);

    BufferedImage getBoundaryMapImage(String boundaryId, int width, int height, boolean drawScale, String scaleLabel);
 }
