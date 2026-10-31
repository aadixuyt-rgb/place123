# Web Guard — Fabric 1.21.4

Mod kliencki Fabric, zawsze aktywny (bez klawisza i GUI).

## Celowanie i kopanie

- **Zwykłe przedmioty:** wszystkie przedmioty poza wyjątkami poniżej ignorują hitboxy sztandarów podczas celowania.
- **Wyjątki od ignorowania sztandarów:** pusta ręka, dowolna siekiera, pajęczyna, dowolny sztandar, elytra, fajerwerki, złote jabłko, zaklęte złote jabłko i Blaze Rod. Przy tych przedmiotach sztandary zachowują normalny hitbox.
- **Nożyce:** przebijają inne bloki, aby kopać wełnę i pajęczyny; celowanie w encje, np. owce, pozostaje normalne.
- **Siekiera:** przebija inne bloki, aby celować i kopać sztandary.

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
