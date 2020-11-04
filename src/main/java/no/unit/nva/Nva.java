package no.unit.nva;

import com.structurizr.Workspace;
import com.structurizr.api.StructurizrClient;
import com.structurizr.api.StructurizrClientException;
import com.structurizr.graphviz.GraphvizAutomaticLayout;
import com.structurizr.model.Container;
import com.structurizr.model.Model;
import com.structurizr.model.SoftwareSystem;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import static java.util.Objects.nonNull;

public class Nva {

    public static final String DATACITE = "Datacite";
    public static final String CROSS_REF = "CrossRef";
    public static final String PUBLICATION_SERVICE = "Publication Service";
    public static final String DOI_INGRESS_SERVICE = "DOI Ingress Service";
    public static final String ORCID = "Orcid";
    public static final String AUTHENTICATION_SERVICE = "Authentication Service";
    public static final String AUTHENTICATION_SOURCE = "Authentication Source";
    public static final String PUBLIC_API = "Public API";
    public static final String SINGLE_PAGE_APP = "Single Page App";
    public static final String DATA_STORAGE = "Data storage";
    public static final String DOI_EGRESS_SERVICE = "Doi Egress Service";
    public static final String LIBRARY_SYSTEM_SERVICE = "Library System Service";
    public static final String FILE_SERVICE = "File Service";
    public static final String BLOB_STORAGE = "Blob storage";
    public static final String NVA_CORE = "NvaCore";
    public static final String CRISTIN_PROJECTS_API = "Cristin projects API";
    public static final String BIBSYS_BARE_ARP = "BIBSYS BARE/ARP";
    public static final String BIBSYS_LIBRARY_SYSTEM = "BIBSYS Library system";
    public static final String NSD_DBH = "NSD DBH";
    public static final String FORWARDS_REQUESTS_TO = "Forwards requests to";
    public static final String PROJECTS_SERVICE = "Projects service";
    public static final String PUBLICATION_CHANNEL_SERVICE = "Publication Channel Service";
    public static final String AWS_SERVERLESS_APPLICATION = "AWS Serverless Application";
    public static final String POST_AUTHENTICATION_SERVICE = "Post Authentication Service";
    public static final String INSTITUTION_SERVICE = "Institution Service";
    private static final String INSTITUTION_SOURCE = "Institution Source";
    public static final String DATA_STREAM = "Data stream service";
    public static final String BUILD_GENERATED_GRAPHVIZ = "./build/generated/graphviz/";

    public static void main(String[] args) throws Exception {
        Workspace workspace = getNvaApplicationWorkspace();
        var model = workspace.getModel();
        var nvaCore = getNvaCoreApplication(model);

        addSoftwareSystems(model, nvaCore);
        addUsers(model, nvaCore);
        addContainers(model, nvaCore);

        createSystemContextView(workspace, nvaCore);
        createContainerView(workspace, nvaCore);
        generateGraphViz(workspace);
        persistToStructurizrDotCom(workspace);
    }

    private static void persistToStructurizrDotCom(Workspace workspace) throws StructurizrClientException {
        var structurizrApiKey = System.getenv("STRUCTURIZR_API_KEY");
        var structurizrSecretKey = System.getenv("STRUCTURIZR_SECRET_KEY");
        var structurizrWorkspaceId = System.getenv("STRUCTURIZR_WORKSPACE_ID");

        if (nonNull(structurizrApiKey) && nonNull(structurizrSecretKey) && nonNull(structurizrWorkspaceId)) {
            var structurizrClient = new StructurizrClient(structurizrApiKey, structurizrSecretKey);
            structurizrClient.putWorkspace(Integer.parseInt(structurizrWorkspaceId), workspace);
        }
    }

    private static void addContainers(Model model, SoftwareSystem softwareSystem) {
        var publicApi = getPublicApi(softwareSystem);
        addSinglePageAppContainer(model, softwareSystem, publicApi);
        Container publicationService = addPublicationServiceContainer(softwareSystem, publicApi);

        addDataStorageContainer(softwareSystem, publicationService);
        var dataStreamService = addDataStreamServiceContainer(softwareSystem, publicationService);

        addDoiIngressServiceContainer(model, softwareSystem, publicApi);

        addDoiEgressServiceContainer(model, softwareSystem, dataStreamService);

        addLibrarySystemServiceContainer(model, softwareSystem, publicApi);

        addProjectServiceContainer(model, softwareSystem, publicApi);

        addPublicationChannelServiceContainer(model, softwareSystem, publicApi);

        addAuthenticationServiceContainer(model, softwareSystem, publicApi);
        addPostAuthenticationService(softwareSystem);

        addInstitutionServiceContainer(model, softwareSystem, publicApi);

        addFileManagementServiceContainer(softwareSystem, publicApi);

        addSearchServiceContainer(softwareSystem, publicApi);
    }

    private static void addSearchServiceContainer(SoftwareSystem softwareSystem, Container userContainer) {
        var searchService = addSearchService(softwareSystem);
        userContainer.uses(searchService, FORWARDS_REQUESTS_TO);
        searchService.uses(Objects.requireNonNull(softwareSystem.getContainerWithName(DATA_STREAM)), "Observes events from");
    }

    private static void addFileManagementServiceContainer(SoftwareSystem softwareSystem, Container userContainer) {
        var fileManagementService = addFileManagementService(softwareSystem);
        userContainer.uses(fileManagementService, FORWARDS_REQUESTS_TO);
    }

    private static void addInstitutionServiceContainer(Model model, SoftwareSystem softwareSystem, Container userContainer) {
        var institutionService = addInstitutionService(model, softwareSystem);
        userContainer.uses(institutionService, FORWARDS_REQUESTS_TO);
    }

    private static void addAuthenticationServiceContainer(Model model, SoftwareSystem softwareSystem, Container userContainer) {
        var authenticationService = addAuthenticationService(model, softwareSystem);
        userContainer.uses(authenticationService, FORWARDS_REQUESTS_TO);
    }

    private static void addPublicationChannelServiceContainer(Model model, SoftwareSystem softwareSystem, Container userContainer) {
        var publicationChannelService = addPublicationChannelService(model, softwareSystem);
        userContainer.uses(publicationChannelService, FORWARDS_REQUESTS_TO);
    }

    private static void addProjectServiceContainer(Model model, SoftwareSystem softwareSystem, Container userContainer) {
        var projectsService = addProjectsService(model, softwareSystem);
        userContainer.uses(projectsService, FORWARDS_REQUESTS_TO);
    }

    private static void addLibrarySystemServiceContainer(Model model, SoftwareSystem softwareSystem, Container userContainer) {
        var librarySystemService = addLibrarySystemService(model, softwareSystem);
        userContainer.uses(librarySystemService, FORWARDS_REQUESTS_TO);
    }

    private static void addDoiEgressServiceContainer(Model model, SoftwareSystem softwareSystem, Container userContainer) {
        var doiEgressService = addDoiEgressService(model, softwareSystem);
        userContainer.uses(doiEgressService, "Listens to events from");
    }

    private static void addDoiIngressServiceContainer(Model model, SoftwareSystem softwareSystem, Container userContainer) {
        var doiIngressService = addDoiIngressService(model, softwareSystem);
        userContainer.uses(doiIngressService, FORWARDS_REQUESTS_TO);
    }

    private static void addDataStorageContainer(SoftwareSystem softwareSystem, Container userContainer) {
        var dataStorage = getDataStorage(softwareSystem);
        userContainer.uses(dataStorage, "Creates, modifies and retrieves data in");
    }

    private static Container addDataStreamServiceContainer(SoftwareSystem softwareSystem, Container usesContainer) {
        var dataStream = getDataStreamService(softwareSystem);
        dataStream.uses(usesContainer, "Observes changes in");
        return dataStream;
    }

    private static Container addPublicationServiceContainer(SoftwareSystem softwareSystem, Container userContainer) {
        var publicationService = getPublicationService(softwareSystem);
        userContainer.uses(publicationService, FORWARDS_REQUESTS_TO);
        userContainer.uses(publicationService, FORWARDS_REQUESTS_TO);
        return publicationService;
    }

    private static void addSinglePageAppContainer(Model model, SoftwareSystem softwareSystem, Container userContainer) {
        var singlePageApp = getSinglePageApp(softwareSystem);
        singlePageApp.uses(userContainer, "Gets data from");
        singlePageApp.uses(Objects.requireNonNull(model.getSoftwareSystemWithName(AUTHENTICATION_SOURCE)), "Requests authentication from");
        singlePageApp.uses(Objects.requireNonNull(model.getSoftwareSystemWithName(ORCID)), "Integrates with");
    }

    private static Container getDataStorage(SoftwareSystem softwareSystem) {
        return softwareSystem.addContainer(DATA_STORAGE, "The data store for NVA", "AWS DynamoDB");
    }

    private static Container getDataStreamService(SoftwareSystem softwareSystem) {
        return softwareSystem.addContainer(DATA_STREAM, "Observer service for the data store", AWS_SERVERLESS_APPLICATION);
    }

    private static Container getPublicationService(SoftwareSystem softwareSystem) {
        return softwareSystem.addContainer(PUBLICATION_SERVICE, "Creates, updates and serves publication data", AWS_SERVERLESS_APPLICATION);
    }

    private static Container getPublicApi(SoftwareSystem softwareSystem) {
        return softwareSystem.addContainer(PUBLIC_API, "The public API for NVA", "AWS API Gateway");
    }

    private static Container getSinglePageApp(SoftwareSystem softwareSystem) {
        return softwareSystem.addContainer(SINGLE_PAGE_APP, "The NVA web application", "React JS");
    }

    private static Container addDoiIngressService(Model model, SoftwareSystem softwareSystem) {
        var doiIngressService = softwareSystem.addContainer(DOI_INGRESS_SERVICE, "Requests and transforms data sourced from third-party DOI metadata services", AWS_SERVERLESS_APPLICATION);
        doiIngressService.uses(Objects.requireNonNull(model.getSoftwareSystemWithName(DATACITE)), "Retrieves publication metdata from");
        doiIngressService.uses(Objects.requireNonNull(model.getSoftwareSystemWithName(CROSS_REF)), "Retrieves publication metdata from");
        doiIngressService.uses(Objects.requireNonNull(softwareSystem.getContainerWithName(PUBLICATION_SERVICE)), "Creates Publications in");
        return doiIngressService;
    }

    private static Container addDoiEgressService(Model model, SoftwareSystem softwareSystem) {
        var doiEgressService = softwareSystem.addContainer(DOI_EGRESS_SERVICE, "Mints DOI and updates metadata in DOI registrar third-party servicess", AWS_SERVERLESS_APPLICATION);
        doiEgressService.uses(Objects.requireNonNull(model.getSoftwareSystemWithName(DATACITE)), "Mints DOIs, updates metdata data for DOIs in");
        doiEgressService.uses(Objects.requireNonNull(softwareSystem.getContainerWithName(PUBLICATION_SERVICE)), "Observes changes in");
        return doiEgressService;
    }

    private static Container addLibrarySystemService(Model model, SoftwareSystem softwareSystem) {
        var librarySystemService = softwareSystem.addContainer(LIBRARY_SYSTEM_SERVICE, "Gets last publication for an author name string", AWS_SERVERLESS_APPLICATION);
        librarySystemService.uses(Objects.requireNonNull(model.getSoftwareSystemWithName(BIBSYS_LIBRARY_SYSTEM)), "Gets last publication for a author name from");
        return librarySystemService;
    }

    private static Container addProjectsService(Model model, SoftwareSystem softwareSystem) {
        var projectsService = softwareSystem.addContainer(PROJECTS_SERVICE, "Gets project information by name string", AWS_SERVERLESS_APPLICATION);
        projectsService.uses(Objects.requireNonNull(model.getSoftwareSystemWithName(CRISTIN_PROJECTS_API)), "Gets project data from");
        return projectsService;
    }

    private static Container addPublicationChannelService(Model model, SoftwareSystem softwareSystem) {
        var publicationChannelService = softwareSystem.addContainer(PUBLICATION_CHANNEL_SERVICE, "Gets publication channel data by ISSN/ISBN", AWS_SERVERLESS_APPLICATION);
        publicationChannelService.uses(Objects.requireNonNull(model.getSoftwareSystemWithName(NSD_DBH)), "Gets publication channel data from");
        return publicationChannelService;
    }

    private static Container addAuthenticationService(Model model, SoftwareSystem softwareSystem) {
        var publicationChannelService = softwareSystem.addContainer(AUTHENTICATION_SERVICE, "Redirects users to authentication source, updates user record", "AWS Cognito");
        publicationChannelService.uses(Objects.requireNonNull(model.getSoftwareSystemWithName(NSD_DBH)), "Gets publication channel data from");
        return publicationChannelService;
    }

    private static void addPostAuthenticationService(SoftwareSystem softwareSystem) {
        var postAuthenticationService = softwareSystem.addContainer(POST_AUTHENTICATION_SERVICE, "Creates user data update request", AWS_SERVERLESS_APPLICATION);
        postAuthenticationService.uses(Objects.requireNonNull(softwareSystem.getContainerWithName(AUTHENTICATION_SERVICE)), "Updates records in");
    }

    private static Container addInstitutionService(Model model, SoftwareSystem softwareSystem) {
        var institutionService = softwareSystem.addContainer(INSTITUTION_SERVICE, "Requests institution by ID", AWS_SERVERLESS_APPLICATION);
        institutionService.uses(Objects.requireNonNull(model.getSoftwareSystemWithName(INSTITUTION_SOURCE)), "Requests institution data from");
        return institutionService;
    }

    private static Container addFileManagementService(SoftwareSystem softwareSystem) {
        var fileManagementService = softwareSystem.addContainer(FILE_SERVICE, "Creates and serves files", AWS_SERVERLESS_APPLICATION);
        var blobStorage = softwareSystem.addContainer(BLOB_STORAGE, "The blob store for NVA", "AWS S3");
        fileManagementService.uses(blobStorage, "Persists and retrieves data in");
        return fileManagementService;
    }


    private static Container addSearchService(SoftwareSystem softwareSystem) {
        var searchService = softwareSystem.addContainer("Search Service", "Searches the published Publications index", AWS_SERVERLESS_APPLICATION);
        var indexService = softwareSystem.addContainer("Index service", "Index persistence for NVA", "AWS Elasticsearch");
        searchService.uses(indexService, "Updates and reads from");
        return searchService;
    }

    private static void addUsers(Model model, SoftwareSystem nvaCore) {
        addAnonymousUser(model, nvaCore);
        addWebCrawlerUser(model, nvaCore);
        addCreatorUser(model, nvaCore);
        addCuratorUser(model, nvaCore);
        addEditorUser(model, nvaCore);
        addAdministratorUser(model, nvaCore);
        addSystemAdministratorUser(model, nvaCore);
        addExternalDataCreatorUser(model, nvaCore, "Student dissertations", "A source of data for student dissertations", "Imports student work data from");
        addExternalDataCreatorUser(model, nvaCore, "Institutional archive", "A source of data for published academic work", "Imports data from");
    }

    private static void addSoftwareSystems(Model model, SoftwareSystem nvaCore) {
        addProjectSource(model, nvaCore);
        addPersonaSource(model, nvaCore);
        addAuthenticationSource(model, nvaCore);
        addPersonIdentifierSource(model, nvaCore);
        addLastPublicationDataSource(model, nvaCore);
        addPublicationChannelSource(model, nvaCore);
        addMetadataSources(model, nvaCore);
        addInstitutionSource(model, nvaCore);
    }

    private static void addMetadataSources(Model model, SoftwareSystem nvaCore) {
        var crossRef = getMetadataSource(CROSS_REF, model);
        nvaCore.uses(crossRef, "Gets publication metadata from");
        var dataCite = getMetadataSource(DATACITE, model);
        nvaCore.uses(dataCite, "Gets, creates and updates publication metadata in");
    }

    private static void generateGraphViz(Workspace workspace) throws Exception {
        File file = getFile();

        var graphvizAutomaticLayout = new GraphvizAutomaticLayout(file);
        graphvizAutomaticLayout.apply(workspace);
    }

    private static File getFile() throws IOException {
        var file = new File(BUILD_GENERATED_GRAPHVIZ);
        boolean created = file.mkdirs();
        if (created) {
            return file;
        } else {
            throw new IOException("Could not create GraphViz output directory");
        }
    }

    private static void createSystemContextView(Workspace workspace, SoftwareSystem nvaCore) {
        var views = workspace.getViews();
        var systemContextView = views.createSystemContextView(nvaCore, "SystemContext", "System Context Diagram for NVA");
        systemContextView.addAllSoftwareSystems();
        systemContextView.addAllPeople();
    }

    private static void createContainerView(Workspace workspace, SoftwareSystem nvaCore) {
        var views = workspace.getViews();
        var containerView = views.createContainerView(nvaCore, "Container view", "Container view for NVA");
        containerView.addAllContainers();
    }

    private static SoftwareSystem getNvaCoreApplication(Model model) {
        return model.addSoftwareSystem(NVA_CORE, "The NVA core application");
    }

    private static void addAuthenticationSource(Model model, SoftwareSystem softwareSystem) {
        var authenticationSource = model.addSoftwareSystem(AUTHENTICATION_SOURCE, "The FEIDE application");
        softwareSystem.uses(authenticationSource, "Authenticates users with");

    }

    private static void addPersonIdentifierSource(Model model, SoftwareSystem softwareSystem) {
        var personIdentifierSource = model.addSoftwareSystem(ORCID, "The Orcid application");
        softwareSystem.uses(personIdentifierSource, "Gets and creates ORCID identifier in");
    }

    private static void addProjectSource(Model model, SoftwareSystem softwareSystem) {
        var projectSource = model.addSoftwareSystem(CRISTIN_PROJECTS_API, "The application serving project data");
        softwareSystem.uses(projectSource, "Gets project data from");
    }

    private static void addPersonaSource(Model model, SoftwareSystem softwareSystem) {
        var personaSource = model.addSoftwareSystem(BIBSYS_BARE_ARP, "The application serving persona data");
        softwareSystem.uses(personaSource, "Gets, creates and updates persona data in");
    }

    private static void addLastPublicationDataSource(Model model, SoftwareSystem softwareSystem) {
        var lastPublicationSource = model.addSoftwareSystem(BIBSYS_LIBRARY_SYSTEM, "The application serving library data");
        softwareSystem.uses(lastPublicationSource, "Gets the last published item data for user in");
    }

    private static SoftwareSystem getMetadataSource(String softwareSystem, Model model) {
        return model.addSoftwareSystem(softwareSystem, "An application serving publication metadata");
    }

    private static void addPublicationChannelSource(Model model, SoftwareSystem softwareSystem) {
        var publicationChannelSource = model.addSoftwareSystem(NSD_DBH, "Application serving publication channel data");
        softwareSystem.uses(publicationChannelSource, "Gets publication channel data from");
    }

    private static void addInstitutionSource(Model model, SoftwareSystem softwareSystem) {
        var institutionSource = model.addSoftwareSystem("Institution Source", "Applicaation");
        softwareSystem.uses(institutionSource, "Gets institution data from");
    }

    private static void addAnonymousUser(Model model, SoftwareSystem softwareSystem) {
        var anonymousUser = model.addPerson("Anonymous User", "An anonymous user of the system");
        anonymousUser.uses(softwareSystem, "Views and searches for publicly available data for publications in");
    }

    private static void addWebCrawlerUser(Model model, SoftwareSystem softwareSystem) {
        var webCrawler = model.addPerson("Web crawler", "A web crawler from e.g. Google, Yandex");
        webCrawler.uses(softwareSystem, "Indexes information from");
    }

    private static void addCreatorUser(Model model, SoftwareSystem softwareSystem) {
        var creator = model.addPerson("Creator", "A user who adds Publications to NVA");
        creator.uses(softwareSystem, "Creates and updates publications they own in");
    }

    private static void addCuratorUser(Model model, SoftwareSystem softwareSystem) {
        var curator = model.addPerson("Curator", "A user who curates NVA publications");
        curator.uses(softwareSystem, "Administers, creates and updates publications in");
    }

    private static void addEditorUser(Model model, SoftwareSystem softwareSystem) {
        var editor = model.addPerson("Editor", "A user who edits NVA publications on behalf of users");
        editor.uses(softwareSystem, "Creates and updates publications in");
    }

    private static void addAdministratorUser(Model model, SoftwareSystem softwareSystem) {
        var administrator = model.addPerson("Aministrator", "A user who administers NVA users on behalf of an institution");
        administrator.uses(softwareSystem, "Administers users and publications in");
    }

    private static void addSystemAdministratorUser(Model model, SoftwareSystem softwareSystem) {
        var systemAdministrator = model.addPerson("System Aministrator", "A user who administers NVA on behalf of the system owner");
        systemAdministrator.uses(softwareSystem, "Administers institutions, users and publications in");
    }

    private static void addExternalDataCreatorUser(Model model, SoftwareSystem softwareSystem, String name, String description, String relationDescription) {
        var user = model.addPerson(name, description);
        user.uses(softwareSystem, relationDescription);
    }

    private static Workspace getNvaApplicationWorkspace() {
        return new Workspace("NVA", "The NVA application, allows users to create and administer their publications");
    }
}
