package org.sola.cs.services.ejbs.claim.businesslogic;

import java.util.List;
import javax.ejb.Local;
import org.sola.cs.services.ejb.refdata.entities.SourceType;
import org.sola.cs.services.ejbs.claim.entities.AdministrativeBoundary;
import org.sola.cs.services.ejbs.claim.entities.Attachment;
import org.sola.cs.services.ejbs.claim.entities.AttachmentBinary;
import org.sola.cs.services.ejbs.claim.entities.AttachmentChunk;
import org.sola.cs.services.ejbs.claim.entities.Claim;
import org.sola.cs.services.ejbs.claim.entities.ClaimPermissions;
import org.sola.cs.services.ejbs.claim.entities.ClaimStatus;
import org.sola.cs.services.ejbs.claim.entities.FieldConstraintType;
import org.sola.cs.services.ejbs.claim.entities.FieldType;
import org.sola.cs.services.ejbs.claim.entities.FieldValueType;
import org.sola.cs.services.ejbs.claim.entities.FormTemplate;
import org.sola.cs.services.ejbs.claim.entities.LandUse;
import org.sola.cs.services.ejbs.claim.entities.RejectionReason;
import org.sola.cs.services.ejbs.claim.entities.Restriction;
import org.sola.services.common.ejbs.AbstractEJBLocal;
import org.sola.cs.services.ejbs.claim.entities.SpatialUnit;

@Local
public interface ClaimEJBLocal extends AbstractEJBLocal {
    List<ClaimStatus> getClaimStatuses(String languageCode);
    ClaimStatus getClaimStatus(String code, String languageCode);
    Claim getClaim(String id);
    Claim getClaimByNumber(String nr);
    List<Claim> getChallengingClaimsByChallengedId(String challengingId);
    Claim saveClaim(Claim claim, String languageCode);
    AttachmentBinary saveAttachment(AttachmentBinary attachment);
    void deleteClaim(String claimId);
    AttachmentBinary saveAttachmentFromChunks(AttachmentBinary attachment);
    AttachmentChunk saveAttachmentChunk(AttachmentChunk chunk);
    AttachmentChunk getAttachmentLastChunk(String attachmentId);
    AttachmentBinary getAttachment(String attachmentId);
    List<AttachmentChunk> getAttachmentChunks(String attachmentId);
    boolean deleteAttachmentChunks(String attachmentId);
    boolean deleteClaimChunks(String claimId);
    int getMaxFileSize();
    int getUploadLimit();
    List<LandUse> getLandUses(String languageCode);
    List<RejectionReason> getRejectionReasons(String languageCode);
    boolean withdrawClaim(String claimId);
    boolean rejectClaim(String claimId, String rejectionReasonCode);
    boolean approveClaimReview(String claimId);
    boolean revertClaimReview(String claimId);
    boolean approveClaimModeration(String claimId);
    boolean assignClaim(String claimId);
    boolean unAssignClaim(String claimId);
    boolean canEditClaim(String claimId);
    boolean canWithdrawClaim(String claimId);
    boolean canRejectClaim(String claimId);
    boolean canApproveClaimReview(String claimId);
    boolean canApproveClaimModeration(String claimId);
    boolean canAssignClaim(String claimId);
    boolean canUnAssignClaim(String claimId);
    boolean canDeleteClaim(String claimId);
    boolean canAddDocumentsToClaim(String claimId);
    boolean canSubmitClaim(String claimId);
    boolean canChallengeClaim(String claimId);
    boolean canRevertClaimReview(String claimId);
    boolean submitClaim(String claimId, String languageCode);
    boolean canPrintClaimCertificate(String claimId, String languageCode);
    boolean canPrintClaimLease(String claimId, String languageCode);
    boolean canIssueCertificate(String ClaimId);
    boolean canRenewCertificate(String ClaimId);
    boolean canTransferClaim(String claimId);
    boolean canCancelCertificate(String claimId);
    boolean renumberClaim(String claimId, String languageCode);
    boolean canRenumberClaim(String claimId);
    void addClaimAttachment(String claimId, String attachmentId);
    Attachment saveClaimAttachment(Attachment attachment, String languageCode);
    ClaimPermissions getClaimPermissions(String claimId);
    List<FormTemplate> getFormTemplates(String languageCode);
    FormTemplate getFormTemplate(String templateName, String languageCode);
    FormTemplate getDefaultFormTemplate(String languageCode);
    List<FieldType> getFieldTypes(String languageCode);
    List<FieldValueType> getFieldValueTypes(String languageCode);
    List<FieldConstraintType> getFieldConstraintTypes(String languageCode);
    boolean checkFormTemplateHasPayload(String formName);
    boolean issueCertificate(String claimId, String languageCode);
    boolean renewCertificate(String claimId, String languageCode);
    List<SourceType> getDocumentTypesForIssuance(String languageCode);
    Claim transferClaim(Claim claim, String languageCode);
    Claim registerMortgage(Claim claim, String languageCode);
    boolean cancelCertificate(String claimId, String languageCode);
    Restriction terminateRestriction(String restrictionId);
    void mergeClaims(List<Claim> oldClaims, Claim newClaim);
    void splitClaim(Claim oldClaim, List<Claim> newClaims);
    AdministrativeBoundary getAdministrativeBoundary(String id);
    List<AdministrativeBoundary> getApprovedAdministrativeBoundaries();
    List<AdministrativeBoundary> getAdministrativeBoundaries(String statusCode, String typeCode);
    AdministrativeBoundary saveAdministrativeBoundary(AdministrativeBoundary boundary);
    void deleteAdministrativeBoundary(String boundaryId);
    boolean approveAdministrativeBoundary(String boundaryId);
    boolean reviseAdministrativeBoundary(String boundaryId);    
    SpatialUnit getSpatialUnit(String id);

    /**
     * See {@linkplain ClaimEJB#createFirstNationSequence(String, String)
     * ClaimEJB.createFirstNationSequence}
     */
    int createFirstNationSequence(String firstNationCode, String firstNationName);
    
    /**
     * See {@linkplain ClaimEJB#generateRecordingNumber(String)
     * ClaimEJB.generateRecordingNumber}
     */
    String generateRecordingNumber(String boundaryId);
}
