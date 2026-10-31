# Web Guard — Fabric 1.21.4

Mod kliencki Fabric, zawsze aktywny (bez klawisza i GUI).

## Celowanie i kopanie

- **Zwykłe przedmioty:** wszystkie przedmioty poza wyjątkami poniżej ignorują hitboxy sztandarów podczas celowania.
- **Wyjątki od ignorowania sztandarów:** pusta ręka, dowolna siekiera, pajęczyna, dowolny sztandar, elytra, fajerwerki, złote jabłko, zaklęte złote jabłko i Blaze Rod. Przy tych przedmiotach sztandary zachowują normalny hitbox.
- **Nożyce i siekiera:** robią tylko jedno — pomijają hitbox gracza stojącego na linii celowania i kopią/celują w to, co faktycznie jest za nim (zwykły blok, bez przebijania innych bloków). Zwierzęta/moby nadal normalnie blokują celownik, więc strzyżenie owcy nożycami i uderzenie moba siekierą działają jak w wanilii — to dotyczy tylko gracza.

## Stawianie

- Wełnę, sztandary i pajęczyny można stawiać przez hitboxy graczy/mobów oraz sztandarów i pajęczyn.
- **Blokada Web + Blaze Rod:** jeśli w głównej ręce (main hand) trzymasz Blaze Rod, a w drugiej (off hand) pajęczynę, mod nie wykonuje specjalnego stawiania pajęczyny z offhandu. Jeśli w main hand trzymasz inny przedmiot, stawianie działa normalnie.

## Pozostałe

- Nie ma funkcji automatycznego używania fajerwerków po przytrzymaniu lewego SHIFT-a.
- Zmiana celowania działa po stronie klienta; serwer nadal zatwierdza akcje i może je ograniczać.

## Build

Java 21: `./gradlew build` → `build/libs/web-guard-1.0.0.jar`.

## Autor

Adixu_YT
