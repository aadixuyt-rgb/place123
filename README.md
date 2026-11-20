# Web Guard — Fabric 1.21.4

Mod kliencki Fabric, zawsze aktywny (bez klawisza i GUI widocznego w ustawieniach — zobacz klawisz `=` niżej).

## Klawisz '=' (wyłącznik główny)

Klawisz **`=`** jest przełącznikiem: za każdym wciśnięciem zamienia stan włączony/wyłączony (jest komunikat na czacie przy każdym przełączeniu). Ten klawisz jest odczytywany bezpośrednio z GLFW, a nie jako zwykły `KeyBinding` — nie pojawia się w Opcje → Sterowanie, nie da się go tam przebindować ani w ogóle zobaczyć.

Po wciśnięciu `=` (stan "wyłączony") **wyłącza się całkowicie**:
- omijanie hitboxu pajęczyny przy biciu/kopaniu,
- omijanie hitboxu sztandaru przy biciu/kopaniu,
- zachowanie nożyc i kilofów (pomijanie hitboxu gracza na linii celowania) — wraca normalne, wanilijskie celowanie/bicie,
- stawianie wełny/sztandaru/pajęczyny przez hitbox gracza/moba — wraca normalne stawianie (nie postawisz przez gracza/moba).

**Działa cały czas, niezależnie od `=`** (nie da się tego wyłączyć):
- blokada Web + Blaze Rod — nie postawisz pajęczyny z offhandu trzymając Blaze Rod w głównej ręce.

Wciśnięcie `=` ponownie włącza wszystko z powrotem.

## Celowanie i kopanie

- **Pajęczyny:** przedmioty inne niż pusta ręka, nożyce i dowolny kilof ignorują hitbox pajęczyny podczas bicia/kopania — ale **tylko wtedy, gdy na tej samej linii celowania, w pajęczynie albo za nią, stoi gracz lub mob**. Jeśli nikogo tam nie ma, pajęczyna ma normalny hitbox. To dotyczy tylko bicia/kopania — stawianie (patrz niżej) nic nie omija.
- **Sztandary:** przedmioty inne niż pusta ręka, nożyce i dowolny kilof ignorują hitbox sztandaru podczas celowania — ale **tylko wtedy, gdy na tej samej linii celowania, na wysokości sztandaru albo dalej, stoi gracz lub mob (zwierzę/potwór)**. Jeśli nikogo tam nie ma, sztandar ma normalny hitbox.
- **Wyjątki od ignorowania sztandarów:** pusta ręka, dowolny kilof, nożyce, pajęczyna, dowolny sztandar, elytra, fajerwerki, złote jabłko, zaklęte złote jabłko i Blaze Rod. Przy tych przedmiotach sztandary zawsze zachowują normalny hitbox.
- **Nożyce i każdy kilof:** pomijają wyłącznie hitbox gracza stojącego na linii celowania i pozwalają celować/kopać w to, co jest za nim. Zwierzęta i inne moby nadal normalnie blokują celownik. Żaden rodzaj siekiery nie ma już tej funkcji.

## Stawianie

- Wełnę, sztandary i pajęczyny można stawiać tylko przez hitboxy graczy/mobów. Sztandar i pajęczyna są przy stawianiu zawsze pełnymi, normalnymi blokami — nic nie jest przez nie pomijane (stawiasz przed nimi, nie za nimi).
- **Blokada Web + Blaze Rod:** jeśli w głównej ręce (main hand) trzymasz Blaze Rod, a w drugiej (off hand) pajęczynę, nie możesz postawić pajęczyny z offhandu. Ta blokada działa zawsze, niezależnie od klawisza `=`. Jeśli w main hand trzymasz inny przedmiot, stawianie działa normalnie.

## Pozostałe

- Nie ma funkcji automatycznego używania fajerwerków po przytrzymaniu lewego SHIFT-a.
- Zmiana celowania działa po stronie klienta; serwer nadal zatwierdza akcje i może je ograniczać.

## Build

Java 21: `./gradlew build` → `build/libs/web-guard-1.0.0.jar`.

## Autor

Adixu_YT
