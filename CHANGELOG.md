# Changelog

Tutte le modifiche rilevanti di LibrePM sono documentate in questo file.

Il formato segue Keep a Changelog e il versionamento segue Semantic Versioning.
Le modifiche in corso devono essere aggiunte sotto `Unreleased`; la pipeline di
release le consolida nella nuova versione e nel changelog Debian.

## [Unreleased]

## [0.1.6] - 2026-08-30

- Maintenance release.

## [0.1.5] - 2026-08-30

- Added: In preparazione: possibilità di connessione remota tramite MariaDB, con creazione automatica dello schema e migrazione amministrativa dal database SQLite locale. La funzione non è ancora integrata nell'interfaccia desktop e il relativo pulsante resta disabilitato.
- Changed: Pipeline desktop allineata alla build versionata e conforme di AssociaGo.
- Changed: Identità tecnica del pacchetto uniformata a `librepm`, senza suffisso `-desktop`.

## [0.1.2] - 2026-08-01

- Prima release LibrePM gestita dalla pipeline di packaging conforme.
