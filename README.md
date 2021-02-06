# myStaff

## Gebruikte versies

* [Play Framework: 2.6.13](https://www.playframework.com/documentation/2.6.x/Home)
* [Angular: 7.x.x](https://angular.io/)
* [Angular CLI: 7.1.4](https://cli.angular.io/)
* [Docker client versie 18.03.0-ce](https://docs.docker.com/toolbox/toolbox_install_windows/)
* [Docker server versie 18.09.2.](https://docs.docker.com/toolbox/toolbox_install_windows/)

### Vereisten

* [Node.js](https://nodejs.org/)
* [JDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html)

### Gebruikte IDE
* Wij gebruikten IDEA Ultimate edition van IntelliJ met de sbt build tool.

## Software te bereiken via browser

Het afgewerkte project is te bereiken via de UGent VPN en vervolgens naar XXXXXXX te surfen. Hierna kan worden ingelogd met de bovenstaande login-gegevens. Indien men zelf het project wil deployen met Docker, staan de stappen hieronder beschreven. Eerst wordt nog een korte uitleg gegeven hoe het project geïnstalleerd moet worden.

## Installatie project

Er zijn zip’s beschikbaar waarin de volledige applicatie zit. Deze kunnen op een server worden geplaatst, waarover later meer. Indien echter nog modificaties nodig zijn, kan men deze doen in een IDE naar keuze, hier zal IntelliJ IDEA gebruikt worden. Deze IDE kan dienen als server in ontwikkelingsfase. Als startproject dient het project van op https://github.ugent.be/bp-vop-2019/mystaff te worden gecloned.

1.	Download & installeer IntelliJ Ultimate(everything)
2.	Copy de link via de repository "https://github.ugent.be/bp-vop-2019/mystaff"
3.	Importeer de map 'MystaffApp' van het project in IntelliJ, niet openen met version control!
4.	Selecteer sbt als build tool, druk meerdere keren op'next' en ten slotte op finish
5.	Hierna zal het project een tijdje compileren. Als het geladen is, dient nog in 'File->Settings->Languages & Frameworks->Play2->Use Play2 Compiler for this project' geselecteerd te worden en vervolgens IntelliJ te herstarten.
6. Voer in de terminal nog het commando 'npm install' uit in de map 'MystaffApp/ui' om de nodige node_modules te laden

### Te gebruiken commando's sbt


```
    sbt clean           # Clean existing build artifacts

    sbt stage           # Build your application from your project’s source directory

    sbt run             # Run both backend and frontend builds in watch mode

    sbt dist            # Build both backend and frontend sources into a single distribution artifact

    sbt test            # Run both backend and frontend unit tests
```



## Deployen met Docker
### Korte beschrijving
Om geen last te hebben van alle verschillende servers, en deze te concentreren op één plaats, kan men gebruikmaken van een Docker container.
Om deze Docker container werkende te krijgen, dient men het project van op GitHub te clonen.
De bestanden die hier nodig zullen zijn, vindt men in de “/docker” map.
Er zal worden gebruikgemaakt van een Linux terminal voor bepaalde commando’s. Makkelijkst is het wanneer men hiervoor de Docker Quickstart Terminal  gebruikt. Deze zal ook gebruikt worden voor het compileren van de finale containers.
Er werd gebruik gemaakt van de Docker client versie 18.03.0-ce en de Docker server versie 18.09.2. Deze versies kan men bekomen via het commando “docker version” in de Docker Quickstart Terminal.

### Front end builden
De front end  kan makkelijk worden gebuild door in IntelliJ in de terminal onderaan zich te begeven naar Mystaffapp/ui en daar het commando “ng build –prod” in te geven. Nu bevindt zich een compacte html- en js-voorstelling van de frontend in de map “GITMAP\MystaffApp\ui\dist\java-play-angular-seed”. Deze kan dan worden gedraaid op een webserver naar keuze.
Wanneer dat gebeurd is, kan men de files van “GITMAP\MystaffApp\ui\dist\java-play-angular-seed” plaatsen in “GITMAP\docker\mystaff-http\src”. 
De laatste versie van het project staat daar met de standaardparameters klaar. 
Zo dient de url in het configuratiebestand “http://192.168.99.100:” te zijn wanneer de front end gelinkt is aan een back end op hetzelfde Windowssysteem draait, dit gebeurt dus enkel in de testfase. Bij Linux kan dit worden ingesteld met als ip-adres “localhost”. Wanneer de back end draait op een andere computer, dient de juiste url naar die webserver te worden gebruikt, bv. “http://bpvop5.ugent.be:”. 

### Back end builden
Ook de back end kan worden gebuild naar een compact archief. Dit doet men door in de sbt terminal onderaan het commando “dist” in te voeren. Na enkele minuten wachten bevat de map “MystaffApp\target\universal” nu het bestand “java-play-angular-seed-1.0-SNAPSHOT”. Dit bestand kan worden geladen op een java-server naar keuze. Belangrijk is dat minstens de JVM arguments “-Xms1G -Xmx2G -Xss4M” worden meegegeven. Zonder Xss zal de recursie niet diep genoeg kunnen gaan en wordt er steeds een stackOverflowException gegooid wanneer de gebruiker de link /newsfeed opent. 

Merk op dat de JVM argumenten niet ingesteld moeten worden wanneer men een docker container gebruikt. Nu dient men de zip bekomen in “GITMAP\MystaffApp\target\universal” te kopiëren en te plaatsen in “GITMAP\docker\mystaff-java". Indien de map src al bestaat, dient deze te worden verwijderd. Nu dient men in een Linux terminal zich te begeven naar de map “GITMAP\docker\mystaff-java” en er het commando “bash unzipJarScript” uit te voeren. Nu bevindt zich in die map in de map “src” de inhoud van de zip, op een speciale manier uitgepakt.
De laatste versie van het project staat daar met de standaardparameters klaar. Indien nodig kunnen enkel de parameters er analoog aan 6.2.3 worden aangepast.
In application.conf hoort voor een Docker container een url analoog aan “jdbc:mysql://192.168.99.100:3306/iiidb” te worden ingegeven indien men werkt in een Windows omgeving. In een Linux omgeving dient men een url zoals “jdbc:mysql://localhost:3306/iiidb” in te geven.

### Algemene Docker opties 

De algemene Docker opties kunnen worden gevonden in “GITMAP \docker\project.yml”.
Hierin kan men de poorten aanpassen waarop bepaalde containers worden geëxporteerd. Ook de login-gegevens van de databank kunnen worden ingesteld. De naam van de database kan ook worden aangepast (standaard “iiidb”) maar dient hetzelfde te zijn als de naam vermeld in het bovenste lijntje van “GITMAP \docker\mystaff-mariadb\init.sql” alsook in eldersvermelde url’s die het woord “iiidb” bevatten. Verder vindt men ook de eerder vermelde JVM argumenten in deze file.

### Starten van de Docker container

Dit is redelijk simpel. Men dient enkel via de Docker Quickstart Terminal zich te begeven naar “GITMAP\docker” en er het commando “bash buildandrun.sh”  uit te voeren. Wanneer het eerder genoemde script geen fouten opgooit, er dus vier keer DONE staat na het uitvoeren van het script, dan is alles normaal gezien in orde.
Merk op dat het opstarten van de Java container iets langer kan duren dan de andere containers. Dit omdat deze moet wachten tot de database is opgestart.
Als alle containers opgestart zijn, moet men enkel nog in de sbt shell het commando 'run' opgeven. Hierna wordt een browservenster geopend met de juiste link. 

## Deployen zonder Docker
### Korte beschrijving

Wanneer het eindproject van op github gebruikt, of men enkele aanpassingen heeft gedaan en gecompileerd via IntelliJ, kan men de http- en de java- files nu deployen op een server.
De http-files bevinden zich onder “GITMAP\MystaffApp\ui\dist\java-play-angular-seed”.
De java-zip is “GITMAP\MystaffApp\target\universal\java-play-angular-seed-1.0-SNAPSHOT”.
De myStaff-applicatie kan draaien op elk besturingssysteem.

### Configuratie van front end

Het configuratiebestand van de front end vindt men onder “assets/config/config.dev.json”. Er zijn slechts twee parameters aanwezig.
-	backend.url: de url naar de back end. Vb. “http://localhost:”. Merk hierop de “http://”  vooraan en de “:” achteraan op.
-	backend.port: de poort van de back end. Vb. “9000”
Wanneer dit ingesteld is, kunnen de files gewoon worden geplaatst op een http-server.

### Configuratie van de back end

Het configuratiebestand van de back end vindt men in de zip als “conf/application.conf”.
Hierin zijn zowel parameters die product specifiek zijn, als parameters die niet moeten worden aangepast. Enkel de product specifieke worden overlopen.

#### Parameters i.v.m. databank
De applicatie heeft een database nodig om te kunnen functioneren. In ons geval gebruiken we MariaDB. Elke andere SQL-gebaseerde databank volstaat ook. Wanneer een SQL-databank naar keuze is opgestart, kan men de juiste tabellen laten genereren met behulp van het opstartscript dat zich bevindt op de gitHub-repo onder de naam: “mystaff/MystaffApp/conf/evolutions/default/1.sql”. De bovenste helft van die file bevindt zich onder de naam “ups” en vormt de reeks commando’s die dienen te worden ingegeven om de nieuwe tabellen te generen. Onderaan, onder de naam “downs” zijn de tegengestelde commando’s te vinden, die de databank opnieuw leegmaken. Wanneer het deel onder ups gebeurd is, is de database klaar voor gebruik. Alternatief kan men ook “mystaff/docker/mystaff-mariadb/init.SQL” runnen. Dit script zorgt ervoor dat de naam van de gebruikte database “iiidb” wordt.

Nu moet er nog communicatie kunnen gebeuren tussen de database en de back-end. Daarom moet men bezitten over de nodige drivers van de gebruikte databank. Deze vindt men snel op Google. Wanneer een JAR-bestand met de driver erin is gevonden, kan men deze plaatsen in de map “mystaff/MystaffApp/lib/”. Wanneer het bestand “mariadb-java-client-2.4.0.jar” nog aanwezig is in deze map, en geen mariadb-databank wordt gebruikt, mag dit verwijderd worden. 

Nu moet de back-end nog weten wat de login-gegevens zijn van de databank. Hiertoe moet “mystaff/MystaffApp/conf/application.conf” worden aangepast. Control-F’en naar “default.driver” brengt je snel naar de nodige plaats. Hier moeten de volgende zaken worden aangepast:

* default.driver: dit heeft te maken met de gebruikte JAR. De naam die hier moet gebruikt worden, vindt men normaal op de plaats waar men de JAR vond. Vb: org.mariadb.jdbc.Driver
* default.url: de connectie url van de database. Deze is afhankelijk van de soort database die men gebruikt, van het ip-adres en de poort waarop ze draait en de naam van de gebruikte databasegroep. Vb:  "jdbc:mysql://192.168.99.100:3306/iiidb"
* default.username en default.password: de gebruikersnaam en het wachtwoord van de gebruikte database

#### Parameters i.v.m. mails
* mails.server: de gebruikte mailserver bv “smtp.gmail.com”
* mails.serverPort: de gebruikte poort van de server bv “587”
* mails.account: het e-mailadres vanwaar de mails worden gestuurd bv “mystaffexample@gmail.com”
* mails.password: het wachtwoord van het bovenstaande account “vakantie@5” 
* mails.doSend: moeten er mails worden gestuurd?
* mails.doSendToPlanner: moeten er mails worden gestuurd naar de planner bij elke nieuwe aanvraag?
* mails.LogSentMails: moet er gelogd worden als er een mail wordt gestuurd onderaan?

#### Parameters i.v.m. Axians 
* axians.tenant: de aangeboden tenant-code door Axians bv “ugent2019”
* axians.standardMaxAbsenceDays: hoeveel dagen een persoon mag verlof nemen per jaar bv “35”
 
#### Andere parameters
* mystaff.doLogRequests: moeten de gedane GET en POST requests worden gelogd?

### Opzetten back end
Om de java back end op te zetten, dient men enkel de zip uit te pakken op de java server. Starten van de server kan dan met het script dat zich bevindt onder de naam: “bin/ java-play-angular-seed.bat”.

## Project structure

De map 'MystaffApp' bevat de volledige applicatie, zowel back end als front end (in submap ui)

### Back end

* De back end is gemaakt met Java Play 2 en zit in de map MystaffApp
* De business logic en controllers zitten in de map 'app'
* De back end routes voor HTTP calls zijn gedefinieerd in het bestand 'MystaffApp/conf/routes'
* De configuraties voor bv. de database zijn gedefinieerd in 'MystaffApp/conf/application.conf'
* Het startup script voor de database zit in 'MystaffApp/conf/evolutions/default/1.sql'
* Testen zitten in de map 'MystaffApp/test'. Deze kunnen worden gerund met het sbt-commando 'testOnly <filename>' bv. testOnly HolidaysBelgiumTest 
* De properties-bestanden om de mails te vertalen zitten in 'MystaffApp/conf/resources/'

### Front end

* De front end maakt gebruik van Angular en bevindt zich in 'MystaffApp/ui'
* De verschillende Angular componenten zitten in 'MystaffApp/ui/src/app'
* De routes zitten in 'MystaffApp/ui/src/app.module.ts'
* De configuratie van de url naar de back end bevindt zich in "MystaffApp/ui/src/assets/config/config.dev.json"
* De ts-bestanden met daarin de vertalingen bevinden zich in 'MystaffApp/ui/src/app/translate/'



