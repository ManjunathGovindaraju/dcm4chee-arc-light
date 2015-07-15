/*
 * **** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at https://github.com/gunterze/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * J4Care.
 * Portions created by the Initial Developer are Copyright (C) 2013
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See @authors listed below
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * **** END LICENSE BLOCK *****
 */

package org.dcm4chee.archive.conf;

import org.dcm4che3.conf.api.DicomConfiguration;
import org.dcm4che3.data.Code;
import org.dcm4che3.data.Issuer;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.imageio.codec.ImageReaderFactory;
import org.dcm4che3.imageio.codec.ImageWriterFactory;
import org.dcm4che3.net.*;
import org.dcm4che3.net.audit.AuditLogger;
import org.dcm4che3.net.audit.AuditRecordRepository;
import org.dcm4che3.net.hl7.HL7Application;
import org.dcm4che3.net.hl7.HL7DeviceExtension;
import org.dcm4che3.net.imageio.ImageReaderExtension;
import org.dcm4che3.net.imageio.ImageWriterExtension;

import java.net.URI;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.EnumSet;

import static org.dcm4che3.net.TransferCapability.Role.SCP;
import static org.dcm4che3.net.TransferCapability.Role.SCU;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @since Jul 2015
 */
class ArchiveDeviceFactory {
    static final String[] OTHER_DEVICES = {
            "dcmqrscp",
            "stgcmtscu",
            "storescp",

            "mppsscp",
            "ianscp",
            "storescu",
            "mppsscu",
            "findscu",
            "getscu",
            "movescu",
            "hl7snd"
    };
    static final String[] OTHER_AES = {
            "DCMQRSCP",
            "STGCMTSCU",
            "STORESCP",
            "MPPSSCP",
            "IANSCP",
            "STORESCU",
            "MPPSSCU",
            "FINDSCU",
            "GETSCU"
    };
    static final Issuer SITE_A =
            new Issuer("Site A", "1.2.40.0.13.1.1.999.111.1111", "ISO");
    static final Issuer SITE_B =
            new Issuer("Site B", "1.2.40.0.13.1.1.999.222.2222", "ISO");
    static final Code INST_B =
            new Code("222.2222", "99DCM4CHEE", null, "Site B");
    static final Issuer[] OTHER_ISSUER = {
            SITE_B, // DCMQRSCP
            null, // STGCMTSCU
            SITE_A, // STORESCP
            SITE_A, // MPPSSCP
            null, // IANSCP
            SITE_A, // STORESCU
            SITE_A, // MPPSSCU
            SITE_A, // FINDSCU
            SITE_A, // GETSCU
    };
    static final Code INST_A =
            new Code("111.1111", "99DCM4CHEE", null, "Site A");
    static final Code[] OTHER_INST_CODES = {
            INST_B, // DCMQRSCP
            null, // STGCMTSCU
            null, // STORESCP
            null, // MPPSSCP
            null, // IANSCP
            INST_A, // STORESCU
            null, // MPPSSCU
            null, // FINDSCU
            null, // GETSCU
    };
    static final int[] OTHER_PORTS = {
            11113, 2763, // DCMQRSCP
            11114, 2765, // STGCMTSCU
            11115, 2766, // STORESCP
            11116, 2767, // MPPSSCP
            11117, 2768, // IANSCP
            Connection.NOT_LISTENING, Connection.NOT_LISTENING, // STORESCU
            Connection.NOT_LISTENING, Connection.NOT_LISTENING, // MPPSSCU
            Connection.NOT_LISTENING, Connection.NOT_LISTENING, // FINDSCU
            Connection.NOT_LISTENING, Connection.NOT_LISTENING, // GETSCU
    };

    static final int[] PATIENT_ATTRS = {
            Tag.SpecificCharacterSet,
            Tag.PatientName,
            Tag.PatientID,
            Tag.IssuerOfPatientID,
            Tag.IssuerOfPatientIDQualifiersSequence,
            Tag.PatientBirthDate,
            Tag.PatientBirthTime,
            Tag.PatientSex,
            Tag.PatientInsurancePlanCodeSequence,
            Tag.PatientPrimaryLanguageCodeSequence,
            Tag.OtherPatientNames,
            Tag.OtherPatientIDsSequence,
            Tag.PatientBirthName,
            Tag.PatientAge,
            Tag.PatientSize,
            Tag.PatientSizeCodeSequence,
            Tag.PatientWeight,
            Tag.PatientAddress,
            Tag.PatientMotherBirthName,
            Tag.MilitaryRank,
            Tag.BranchOfService,
            Tag.MedicalRecordLocator,
            Tag.MedicalAlerts,
            Tag.Allergies,
            Tag.CountryOfResidence,
            Tag.RegionOfResidence,
            Tag.PatientTelephoneNumbers,
            Tag.EthnicGroup,
            Tag.Occupation,
            Tag.SmokingStatus,
            Tag.AdditionalPatientHistory,
            Tag.PregnancyStatus,
            Tag.LastMenstrualDate,
            Tag.PatientReligiousPreference,
            Tag.PatientSpeciesDescription,
            Tag.PatientSpeciesCodeSequence,
            Tag.PatientSexNeutered,
            Tag.PatientBreedDescription,
            Tag.PatientBreedCodeSequence,
            Tag.BreedRegistrationSequence,
            Tag.ResponsiblePerson,
            Tag.ResponsiblePersonRole,
            Tag.ResponsibleOrganization,
            Tag.PatientComments,
            Tag.ClinicalTrialSponsorName,
            Tag.ClinicalTrialProtocolID,
            Tag.ClinicalTrialProtocolName,
            Tag.ClinicalTrialSiteID,
            Tag.ClinicalTrialSiteName,
            Tag.ClinicalTrialSubjectID,
            Tag.ClinicalTrialSubjectReadingID,
            Tag.PatientIdentityRemoved,
            Tag.DeidentificationMethod,
            Tag.DeidentificationMethodCodeSequence,
            Tag.ClinicalTrialProtocolEthicsCommitteeName,
            Tag.ClinicalTrialProtocolEthicsCommitteeApprovalNumber,
            Tag.SpecialNeeds,
            Tag.PertinentDocumentsSequence,
            Tag.PatientState,
            Tag.PatientClinicalTrialParticipationSequence,
            Tag.ConfidentialityConstraintOnPatientDataDescription
    };

    static final int[] STUDY_ATTRS = {
            Tag.SpecificCharacterSet,
            Tag.StudyDate,
            Tag.StudyTime,
            Tag.AccessionNumber,
            Tag.IssuerOfAccessionNumberSequence,
            Tag.ReferringPhysicianName,
            Tag.StudyDescription,
            Tag.ProcedureCodeSequence,
            Tag.PatientAge,
            Tag.PatientSize,
            Tag.PatientSizeCodeSequence,
            Tag.PatientWeight,
            Tag.Occupation,
            Tag.AdditionalPatientHistory,
            Tag.PatientSexNeutered,
            Tag.StudyInstanceUID,
            Tag.StudyID
    };
    static final int[] SERIES_ATTRS = {
            Tag.SpecificCharacterSet,
            Tag.Modality,
            Tag.Manufacturer,
            Tag.InstitutionName,
            Tag.InstitutionCodeSequence,
            Tag.StationName,
            Tag.SeriesDescription,
            Tag.InstitutionalDepartmentName,
            Tag.PerformingPhysicianName,
            Tag.ManufacturerModelName,
            Tag.ReferencedPerformedProcedureStepSequence,
            Tag.BodyPartExamined,
            Tag.SeriesInstanceUID,
            Tag.SeriesNumber,
            Tag.Laterality,
            Tag.PerformedProcedureStepStartDate,
            Tag.PerformedProcedureStepStartTime,
            Tag.RequestAttributesSequence
    };
    static final int[] INSTANCE_ATTRS = {
            Tag.SpecificCharacterSet,
            Tag.ImageType,
            Tag.SOPClassUID,
            Tag.SOPInstanceUID,
            Tag.ContentDate,
            Tag.ContentTime,
            Tag.ReferencedSeriesSequence,
            Tag.InstanceNumber,
            Tag.NumberOfFrames,
            Tag.Rows,
            Tag.Columns,
            Tag.BitsAllocated,
            Tag.ObservationDateTime,
            Tag.ConceptNameCodeSequence,
            Tag.VerifyingObserverSequence,
            Tag.ReferencedRequestSequence,
            Tag.CompletionFlag,
            Tag.VerificationFlag,
            Tag.DocumentTitle,
            Tag.MIMETypeOfEncapsulatedDocument,
            Tag.ContentLabel,
            Tag.ContentDescription,
            Tag.PresentationCreationDate,
            Tag.PresentationCreationTime,
            Tag.ContentCreatorName,
            Tag.OriginalAttributesSequence,
            Tag.IdenticalDocumentsSequence,
            Tag.CurrentRequestedProcedureEvidenceSequence
    };
    static final String[] IMAGE_CUIDS = {
            UID.ComputedRadiographyImageStorage,
            UID.DigitalXRayImageStorageForPresentation,
            UID.DigitalXRayImageStorageForProcessing,
            UID.DigitalMammographyXRayImageStorageForPresentation,
            UID.DigitalMammographyXRayImageStorageForProcessing,
            UID.DigitalIntraOralXRayImageStorageForPresentation,
            UID.DigitalIntraOralXRayImageStorageForProcessing,
            UID.CTImageStorage,
            UID.EnhancedCTImageStorage,
            UID.UltrasoundMultiFrameImageStorageRetired,
            UID.UltrasoundMultiFrameImageStorage,
            UID.MRImageStorage,
            UID.EnhancedMRImageStorage,
            UID.EnhancedMRColorImageStorage,
            UID.NuclearMedicineImageStorageRetired,
            UID.UltrasoundImageStorageRetired,
            UID.UltrasoundImageStorage,
            UID.EnhancedUSVolumeStorage,
            UID.SecondaryCaptureImageStorage,
            UID.MultiFrameGrayscaleByteSecondaryCaptureImageStorage,
            UID.MultiFrameGrayscaleWordSecondaryCaptureImageStorage,
            UID.MultiFrameTrueColorSecondaryCaptureImageStorage,
            UID.XRayAngiographicImageStorage,
            UID.EnhancedXAImageStorage,
            UID.XRayRadiofluoroscopicImageStorage,
            UID.EnhancedXRFImageStorage,
            UID.XRayAngiographicBiPlaneImageStorageRetired,
            UID.XRay3DAngiographicImageStorage,
            UID.XRay3DCraniofacialImageStorage,
            UID.BreastTomosynthesisImageStorage,
            UID.IntravascularOpticalCoherenceTomographyImageStorageForPresentation,
            UID.IntravascularOpticalCoherenceTomographyImageStorageForProcessing,
            UID.NuclearMedicineImageStorage,
            UID.VLEndoscopicImageStorage,
            UID.VLMicroscopicImageStorage,
            UID.VLSlideCoordinatesMicroscopicImageStorage,
            UID.VLPhotographicImageStorage,
            UID.OphthalmicPhotography8BitImageStorage,
            UID.OphthalmicPhotography16BitImageStorage,
            UID.OphthalmicTomographyImageStorage,
            UID.VLWholeSlideMicroscopyImageStorage,
            UID.PositronEmissionTomographyImageStorage,
            UID.EnhancedPETImageStorage,
            UID.RTImageStorage,
    };
    static final String[] IMAGE_TSUIDS = {
            UID.ImplicitVRLittleEndian,
            UID.ExplicitVRLittleEndian,
            UID.DeflatedExplicitVRLittleEndian,
            UID.ExplicitVRBigEndianRetired,
            UID.JPEGBaseline1,
            UID.JPEGExtended24,
            UID.JPEGLossless,
            UID.JPEGLosslessNonHierarchical14,
            UID.JPEGLSLossless,
            UID.JPEGLSLossyNearLossless,
            UID.JPEG2000LosslessOnly,
            UID.JPEG2000,
            UID.RLELossless
    };
    static final String[] VIDEO_CUIDS = {
            UID.VideoEndoscopicImageStorage,
            UID.VideoMicroscopicImageStorage,
            UID.VideoPhotographicImageStorage,
    };
    static final String[] OTHER_CUIDS = {
            UID.MRSpectroscopyStorage,
            UID.MultiFrameSingleBitSecondaryCaptureImageStorage,
            UID.StandaloneOverlayStorageRetired,
            UID.StandaloneCurveStorageRetired,
            UID.TwelveLeadECGWaveformStorage,
            UID.GeneralECGWaveformStorage,
            UID.AmbulatoryECGWaveformStorage,
            UID.HemodynamicWaveformStorage,
            UID.CardiacElectrophysiologyWaveformStorage,
            UID.BasicVoiceAudioWaveformStorage,
            UID.GeneralAudioWaveformStorage,
            UID.ArterialPulseWaveformStorage,
            UID.RespiratoryWaveformStorage,
            UID.StandaloneModalityLUTStorageRetired,
            UID.StandaloneVOILUTStorageRetired,
            UID.GrayscaleSoftcopyPresentationStateStorageSOPClass,
            UID.ColorSoftcopyPresentationStateStorageSOPClass,
            UID.PseudoColorSoftcopyPresentationStateStorageSOPClass,
            UID.BlendingSoftcopyPresentationStateStorageSOPClass,
            UID.XAXRFGrayscaleSoftcopyPresentationStateStorage,
            UID.RawDataStorage,
            UID.SpatialRegistrationStorage,
            UID.SpatialFiducialsStorage,
            UID.DeformableSpatialRegistrationStorage,
            UID.SegmentationStorage,
            UID.SurfaceSegmentationStorage,
            UID.RealWorldValueMappingStorage,
            UID.StereometricRelationshipStorage,
            UID.LensometryMeasurementsStorage,
            UID.AutorefractionMeasurementsStorage,
            UID.KeratometryMeasurementsStorage,
            UID.SubjectiveRefractionMeasurementsStorage,
            UID.VisualAcuityMeasurementsStorage,
            UID.SpectaclePrescriptionReportStorage,
            UID.OphthalmicAxialMeasurementsStorage,
            UID.IntraocularLensCalculationsStorage,
            UID.MacularGridThicknessAndVolumeReportStorage,
            UID.OphthalmicVisualFieldStaticPerimetryMeasurementsStorage,
            UID.BasicStructuredDisplayStorage,
            UID.BasicTextSRStorage,
            UID.EnhancedSRStorage,
            UID.ComprehensiveSRStorage,
            UID.ProcedureLogStorage,
            UID.MammographyCADSRStorage,
            UID.KeyObjectSelectionDocumentStorage,
            UID.ChestCADSRStorage,
            UID.XRayRadiationDoseSRStorage,
            UID.ColonCADSRStorage,
            UID.ImplantationPlanSRStorage,
            UID.EncapsulatedPDFStorage,
            UID.EncapsulatedCDAStorage,
            UID.StandalonePETCurveStorageRetired,
            UID.RTDoseStorage,
            UID.RTStructureSetStorage,
            UID.RTBeamsTreatmentRecordStorage,
            UID.RTPlanStorage,
            UID.RTBrachyTreatmentRecordStorage,
            UID.RTTreatmentSummaryRecordStorage,
            UID.RTIonPlanStorage,
            UID.RTIonBeamsTreatmentRecordStorage,
    };
    static final String[] VIDEO_TSUIDS = {
            UID.JPEGBaseline1,
            UID.MPEG2,
            UID.MPEG2MainProfileHighLevel,
            UID.MPEG4AVCH264BDCompatibleHighProfileLevel41,
            UID.MPEG4AVCH264HighProfileLevel41
    };
    static final String[] OTHER_TSUIDS = {
            UID.ImplicitVRLittleEndian,
            UID.ExplicitVRLittleEndian,
            UID.DeflatedExplicitVRLittleEndian,
            UID.ExplicitVRBigEndianRetired,
    };
    static final String[] QUERY_CUIDS = {
            UID.PatientRootQueryRetrieveInformationModelFIND,
            UID.StudyRootQueryRetrieveInformationModelFIND,
            UID.PatientStudyOnlyQueryRetrieveInformationModelFINDRetired,
            UID.ModalityWorklistInformationModelFIND
    };
    static final String[] RETRIEVE_CUIDS = {
            UID.PatientRootQueryRetrieveInformationModelGET,
            UID.PatientRootQueryRetrieveInformationModelMOVE,
            UID.StudyRootQueryRetrieveInformationModelGET,
            UID.StudyRootQueryRetrieveInformationModelMOVE,
            UID.PatientStudyOnlyQueryRetrieveInformationModelGETRetired,
            UID.PatientStudyOnlyQueryRetrieveInformationModelMOVERetired
    };
    static final String[] HL7_MESSAGE_TYPES = {
            "ADT^A02",
            "ADT^A03",
            "ADT^A06",
            "ADT^A07",
            "ADT^A08",
            "ADT^A40",
            "ORM^O01"
    };
    static final String DCM4CHEE_ARC_KEY_JKS =  "${jboss.server.config.url}/dcm4chee-arc/key.jks";
    static final String HL7_ADT2DCM_XSL = "${jboss.server.config.url}/dcm4chee-arc/hl7-adt2dcm.xsl";
    static final String PIX_CONSUMER = "DCM4CHEE^DCM4CHEE";
    static final String PIX_MANAGER = "HL7RCV^DCM4CHEE";

    static final String STORAGE_ID = "local";
    static final URI STORAGE_URI = URI.create("file:///var/local/dcm4chee-arc/fs1/");
    static final String PATH_FORMAT = "{now,date,yyyy/MM/dd}/{0020000D,hash}/{0020000E,hash}/{00080018,hash}";

    private final KeyStore keyStore;
    private final DicomConfiguration config;

    public ArchiveDeviceFactory(KeyStore keyStore, DicomConfiguration config) {
        this.keyStore = keyStore;
        this.config = config;
    }

    public Device createARRDevice(String name, Connection.Protocol protocol, int port) {
        Device arrDevice = new Device(name);
        AuditRecordRepository arr = new AuditRecordRepository();
        arrDevice.addDeviceExtension(arr);
        Connection auditUDP = new Connection("audit-udp", "localhost", port);
        auditUDP.setProtocol(protocol);
        arrDevice.addConnection(auditUDP);
        arr.addConnection(auditUDP);
        return arrDevice ;
    }

    public Device createDevice(String name) throws Exception {
        return init(new Device(name), null, null);
    }

    public Device createDevice(String name, Issuer issuer, Code institutionCode) throws Exception {
        return init(new Device(name), issuer, institutionCode);
    }

    private Device init(Device device, Issuer issuer, Code institutionCode) throws Exception {
        String name = device.getDeviceName();
        device.setThisNodeCertificates(config.deviceRef(name), (X509Certificate) keyStore.getCertificate(name));
        device.setIssuerOfPatientID(issuer);
        device.setIssuerOfAccessionNumber(issuer);
        if (institutionCode != null) {
            device.setInstitutionNames(institutionCode.getCodeMeaning());
            device.setInstitutionCodes(institutionCode);
        }
        return device;
    }

    public Device createDevice(String name, Issuer issuer, Code institutionCode, String aet,
                               String host, int port, int tlsPort) throws Exception {
        Device device = init(new Device(name), issuer, institutionCode);
        ApplicationEntity ae = new ApplicationEntity(aet);
        ae.setAssociationAcceptor(true);
        device.addApplicationEntity(ae);
        Connection dicom = new Connection("dicom", host, port);
        device.addConnection(dicom);
        ae.addConnection(dicom);
        Connection dicomTLS = new Connection("dicom-tls", host, tlsPort);
        dicomTLS.setTlsCipherSuites(
                Connection.TLS_RSA_WITH_AES_128_CBC_SHA,
                Connection.TLS_RSA_WITH_3DES_EDE_CBC_SHA);
        device.addConnection(dicomTLS);
        ae.addConnection(dicomTLS);
        return device;
    }

    public Device createHL7Device(String name, Issuer issuer, Code institutionCode, String appName,
                                     String host, int port, int tlsPort) throws Exception {
        Device device = new Device(name);
        HL7DeviceExtension hl7Device = new HL7DeviceExtension();
        device.addDeviceExtension(hl7Device);
        init(device, issuer, institutionCode);
        HL7Application hl7app = new HL7Application(appName);
        hl7Device.addHL7Application(hl7app);
        Connection hl7 = new Connection("hl7", host, port);
        hl7.setProtocol(Connection.Protocol.HL7);
        device.addConnection(hl7);
        hl7app.addConnection(hl7);
        Connection hl7TLS = new Connection("hl7-tls", host, tlsPort);
        hl7TLS.setProtocol(Connection.Protocol.HL7);
        hl7TLS.setTlsCipherSuites(
                Connection.TLS_RSA_WITH_AES_128_CBC_SHA,
                Connection.TLS_RSA_WITH_3DES_EDE_CBC_SHA);
        device.addConnection(hl7TLS);
        hl7app.addConnection(hl7TLS);
        return device;
    }
    public Device createArchiveDevice(String name, Device arrDevice) throws Exception {
        Device device = new Device(name);

        Connection dicom = new Connection("dicom", "localhost", 11112);
        dicom.setBindAddress("0.0.0.0");
        dicom.setMaxOpsInvoked(0);
        dicom.setMaxOpsPerformed(0);
        device.addConnection(dicom);

        Connection dicomTLS = new Connection("dicom-tls", "localhost", 2762);
        dicomTLS.setBindAddress("0.0.0.0");
        dicomTLS.setMaxOpsInvoked(0);
        dicomTLS.setMaxOpsPerformed(0);
        dicomTLS.setTlsCipherSuites(
                Connection.TLS_RSA_WITH_AES_128_CBC_SHA,
                Connection.TLS_RSA_WITH_3DES_EDE_CBC_SHA);
        device.addConnection(dicomTLS);

        addArchiveDeviceExtension(device);
        addHL7DeviceExtension(device);
        addAuditLogger(device, arrDevice);
        device.addDeviceExtension(new ImageReaderExtension(ImageReaderFactory.getDefault()));
        device.addDeviceExtension(new ImageWriterExtension(ImageWriterFactory.getDefault()));

        device.setManufacturer("dcm4che.org");
        device.setManufacturerModelName("dcm4chee-arc");
        device.setSoftwareVersions("5.0.0");
        device.setKeyStoreURL(DCM4CHEE_ARC_KEY_JKS);
        device.setKeyStoreType("JKS");
        device.setKeyStorePin("secret");
        device.setThisNodeCertificates(config.deviceRef(name),
                (X509Certificate) keyStore.getCertificate(name));
        for (String other : OTHER_DEVICES)
            device.setAuthorizedNodeCertificates(config.deviceRef(other),
                    (X509Certificate) keyStore.getCertificate(other));

        device.addApplicationEntity(createAE("DCM4CHEE", dicom, dicomTLS, true, true));

        return device;
    }

    private static void addAuditLogger(Device device, Device arrDevice) {
        Connection auditUDP = new Connection("audit-udp", "localhost");
        auditUDP.setProtocol(Connection.Protocol.SYSLOG_UDP);
        device.addConnection(auditUDP);

        AuditLogger auditLogger = new AuditLogger();
        device.addDeviceExtension(auditLogger);
        auditLogger.addConnection(auditUDP);
        auditLogger.setAuditSourceTypeCodes("4");
        auditLogger.setAuditRecordRepositoryDevice(arrDevice);
    }

    private static void addHL7DeviceExtension(Device device) {
        HL7DeviceExtension ext = new HL7DeviceExtension();
        device.addDeviceExtension(ext);

        Connection hl7 = new Connection("hl7", "localhost", 2575);
        hl7.setBindAddress("0.0.0.0");
        hl7.setProtocol(Connection.Protocol.HL7);
        device.addConnection(hl7);

        Connection hl7TLS = new Connection("hl7-tls", "localhost", 12575);
        hl7TLS.setBindAddress("0.0.0.0");
        hl7TLS.setProtocol(Connection.Protocol.HL7);
        hl7TLS.setTlsCipherSuites(
                Connection.TLS_RSA_WITH_AES_128_CBC_SHA,
                Connection.TLS_RSA_WITH_3DES_EDE_CBC_SHA);
        device.addConnection(hl7TLS);

        HL7Application hl7App = new HL7Application("*");
        ArchiveHL7ApplicationExtension hl7AppExt = new ArchiveHL7ApplicationExtension();
        hl7App.addHL7ApplicationExtension(hl7AppExt);
        hl7App.setAcceptedMessageTypes(HL7_MESSAGE_TYPES);
        hl7App.setHL7DefaultCharacterSet("8859/1");
        hl7AppExt.addTemplatesURI("adt2dcm", HL7_ADT2DCM_XSL);
        ext.addHL7Application(hl7App);
        hl7App.addConnection(hl7);
        hl7App.addConnection(hl7TLS);
    }

    private static void addArchiveDeviceExtension(Device device) {
        ArchiveDeviceExtension ext = new ArchiveDeviceExtension();
        device.addDeviceExtension(ext);
        ext.setFuzzyAlgorithmClass("org.dcm4che3.soundex.ESoundex");

        ext.setAttributeFilter(Entity.Patient,
                new AttributeFilter(PATIENT_ATTRS));
        ext.setAttributeFilter(Entity.Study,
                new AttributeFilter(STUDY_ATTRS));
        ext.setAttributeFilter(Entity.Series,
                new AttributeFilter(SERIES_ATTRS));
        ext.setAttributeFilter(Entity.Instance,
                new AttributeFilter(INSTANCE_ATTRS));

        StorageDescriptor storageDescriptor = new StorageDescriptor(STORAGE_ID);
        storageDescriptor.setStorageURI(STORAGE_URI);
        storageDescriptor.setProperty("pathFormat", PATH_FORMAT);
        ext.addStorageDescriptor(storageDescriptor);
    }

    private static ApplicationEntity createAE(String aet, Connection dicom, Connection dicomTLS,
                                              boolean storeSCP, boolean pixQuery) {
        ApplicationEntity ae = new ApplicationEntity(aet);
        ae.addConnection(dicom);
        ae.addConnection(dicomTLS);

        ArchiveAEExtension aeExt = new ArchiveAEExtension();
        ae.addAEExtension(aeExt);
        ae.setAssociationAcceptor(true);
        ae.setAssociationInitiator(true);
        if (storeSCP) {
            addTCs(ae, null, SCP, IMAGE_CUIDS, IMAGE_TSUIDS);
            addTCs(ae, null, SCP, VIDEO_CUIDS, VIDEO_TSUIDS);
            addTCs(ae, null, SCP, OTHER_CUIDS, OTHER_TSUIDS);
            aeExt.setStorageID(STORAGE_ID);
        }
        addTCs(ae, null, SCU, IMAGE_CUIDS, IMAGE_TSUIDS);
        addTCs(ae, null, SCU, VIDEO_CUIDS, VIDEO_TSUIDS);
        addTCs(ae, null, SCU, OTHER_CUIDS, OTHER_TSUIDS);
        addTCs(ae, EnumSet.allOf(QueryOption.class), SCP, QUERY_CUIDS, UID.ImplicitVRLittleEndian);
        addTCs(ae, EnumSet.of(QueryOption.RELATIONAL), SCP, RETRIEVE_CUIDS, UID.ImplicitVRLittleEndian);
        addTC(ae, null, SCP, UID.CompositeInstanceRetrieveWithoutBulkDataGET, UID.ImplicitVRLittleEndian);
        addTC(ae, null, SCP, UID.StorageCommitmentPushModelSOPClass, UID.ImplicitVRLittleEndian);
        addTC(ae, null, SCP, UID.ModalityPerformedProcedureStepSOPClass, UID.ImplicitVRLittleEndian);
        addTC(ae, null, SCU, UID.ModalityPerformedProcedureStepSOPClass, UID.ImplicitVRLittleEndian);
        addTC(ae, null, SCU, UID.InstanceAvailabilityNotificationSOPClass, UID.ImplicitVRLittleEndian);
        addTC(ae, null, SCP, UID.VerificationSOPClass, UID.ImplicitVRLittleEndian);
        addTC(ae, null, SCU, UID.VerificationSOPClass, UID.ImplicitVRLittleEndian);
        aeExt.setPixQuery(pixQuery);
        aeExt.setLocalPIXConsumerApplication(PIX_CONSUMER);
        aeExt.setRemotePIXManagerApplication(PIX_MANAGER);
        return ae;
    }

    private static void addTCs(ApplicationEntity ae, EnumSet<QueryOption> queryOpts,
                        TransferCapability.Role role, String[] cuids, String... tss) {
        for (String cuid : cuids)
            addTC(ae, queryOpts, role, cuid, tss);
    }

    private static void addTC(ApplicationEntity ae, EnumSet<QueryOption> queryOpts,
                       TransferCapability.Role role, String cuid, String... tss) {
        String name = UID.nameOf(cuid).replace('/', ' ');
        TransferCapability tc = new TransferCapability(name + ' ' + role, cuid, role, tss);
        tc.setQueryOptions(queryOpts);
        ae.addTransferCapability(tc);
    }
}
