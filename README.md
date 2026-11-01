# Web Guard — Fabric 1.21.4

Mod kliencki Fabric, zawsze aktywny (bez klawisza i GUI widocznego w ustawieniach — zobacz klawisz `=` niżej).

## Klawisz '=' (wyłącznik główny)

Klawisz **`=`** włącza/wyłącza wszystko opisane w tym pliku **oprócz** blokady Web + Blaze Rod (patrz niżej), która działa zawsze. Ten klawisz jest odczytywany bezpośrednio z GLFW, a nie jako zwykły `KeyBinding` — nie pojawia się w Opcje → Sterowanie i nie da się go tam przebindować ani podejrzeć.

## Celowanie i kopanie

- **Pajęczyny:** wszystkie przedmioty oprócz pustej ręki i nożyc ignorują hitbox pajęczyny podczas celowania/kopania (celujesz/kopiesz w to, co jest za pajęczyną).
- **Sztandary:** wszystkie przedmioty poza wyjątkami poniżej ignorują hitbox sztandaru podczas celowania — ale **tylko wtedy, gdy na tej samej linii celowania, na wysokości sztandaru albo dalej, stoi gracz lub mob (zwierzę/potwór)**. Jeśli nikogo tam nie ma, sztandar ma normalny hitbox.
- **Wyjątki od ignorowania sztandarów:** pusta ręka, dowolna siekiera, pajęczyna, dowolny sztandar, elytra, fajerwerki, złote jabłko, zaklęte złote jabłko i Blaze Rod. Przy tych przedmiotach sztandary zawsze zachowują normalny hitbox (ale pajęczyny nadal są ignorowane zgodnie z regułą wyżej, poza nożycami).
- **Nożyce i siekiera:** robią tylko jedno — pomijają hitbox gracza stojącego na linii celowania i kopią/celują w to, co faktycznie jest za nim. Zwierzęta/moby nadal normalnie blokują celownik, więc strzyżenie owcy nożycami i uderzenie moba siekierą działają jak w wanilii — to dotyczy tylko gracza. (Siekiera dodatkowo ignoruje pajęczyny jak w regule wyżej; nożyce nigdy nie ignorują pajęczyn.)

## Stawianie

- Wełnę, sztandary i pajęczyny można stawiać przez hitboxy graczy/mobów, sztandarów i pajęczyn.
- **Wyjątek:** gdy stawiasz pajęczynę, hitbox *już postawionej* pajęczyny nie jest pomijany — pajęczyna zatrzymuje się na istniejącej pajęczynie zamiast przez nią przechodzić (np. jeśli masz już pajęczynę na nogach, nowa nie przeleci przez nią na inne miejsce).
- **Blokada Web + Blaze Rod:** jeśli w głównej ręce (main hand) trzymasz Blaze Rod, a w drugiej (off hand) pajęczynę, nie możesz postawić pajęczyny z offhandu. Ta blokada działa zawsze, niezależnie od klawisza `=`. Jeśli w main hand trzymasz inny przedmiot, stawianie działa normalnie.

## Pozostałe

- Nie ma funkcji automatycznego używania fajerwerków po przytrzymaniu lewego SHIFT-a.
- Zmiana celowania działa po stronie klienta; serwer nadal zatwierdza akcje i może je ograniczać.

## Build

Java 21: `./gradlew build` → `build/libs/web-guard-1.0.0.jar`.

## Autor

Adixu_YT
