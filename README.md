## Tema 2 Analiza Algoritmilor - CSAT <=p CNF SAT

**Student** : Denis-Marian Vlădulescu\
**Grupa** : 324CB\
**Limbaj de programare utilizat**: Java

### Descrierea implementării

Structurile de date folosite sunt :
- GateTree, clasă specializată pentru a reprezenta o poartă logică și copiii(input-urile) acesteia
- ArrayList, pentru a stoca copiii(input-urile) unei porți

Pentru rezolvarea reducerii am urmat pașii :
- Citesc din fișierul de input datele despre problema CSAT : numărul de variabile de intrare și indicele variabileide output finale
- Citesc din fișierul de input datele despre porțile logice din care este format circuitul și le stochez într-o matrice cu dimensiuni generice conform cerinței
- Parcurg matricea creată anterior și o redimensionez pentru a se potrivi pe datele cazului curent
- Mă folosesc de metodele ```findNodeLine``` și ```buildTree``` pentru a găsi linia pe care se află descrierea porților logice, respectiv pentru a construi arborele asociat circuitului
- După construirea arborelui verific dacă rădăcina este o poartă de tip ```OR```, iar în caz afirmativ verific dacă, printre copiii rădăcinii, există o variabilă și varianta negată a acesteia. În caz afirmativ, se încheie execuția întrucât problema va fi satisfiabilă indiferent de valorile luate de variabile, deoarece atunci când variabila în cauză este falsă, contrarul va fi fals și invers
- În următorii pași am să combin porțile ```AND```/```OR``` imbricate pentru a simplifica procesul mai departe
- Apoi apelez metoda ```convertToCNF``` care va converti arborele CSAT într-o expresie CNF astfel :
    - Parcurg arborele recursiv plecând din rădăcină, în adâncime pentru a avea o abordare Bottom-Up
    - Dacă întâlnesc o variabilă, înseamnă că ramura respectivă a ajuns la final. Dacă întâlnesc o poartă ```AND```, verific, pe rând, pentru fiecare copil, dacă este variabilă, caz în care îl las nemodificat, altfel se realizează apelul recursiv pe copilul respectiv. Analog pentru o poartă ```OR```, la care se adaugă verificarea dacă această poartă conține măcar un copil de tip ```AND```, caz în care apelez metoda ```distributeOR```, al cărei scop este să distribuie poarta ```OR``` peste poarta ```AND```, aplicând distributivitatea și astfel obținând o expresie CNF SAT
    - La final, se returnează rădăcina, eventual, modificată
- După ce am obținut expresia CNF SAT, combin orice porți ```OR``` sau ```AND``` rămase, dacă sunt, iar apoi realizez afișarea formulei generate

Un aspect important de menționat este că în arborele inițial am ales să nu stochez și porțile ```NOT``` întrucât :
- Ar fi complicat procesul de trecere la formulă CNF SAT, deoarece întâi ar fi trebuit eliminate aceste porți
- Ele pot fi înlocuite cu copilul porții, negat. Dacă copilul este o variabilă, stochez direct variabila negată. Dacă copilul este o poartă ```AND```, o stochez ca o poartă ```OR``` și neg toate expresiile din copiii acesteia. Analog, o poartă ```OR``` o stochez sub forma unei porți ```AND``` și neg toate expresiile din copiii acesteia.

### Detalii de implementare reducere SAT <=p CNFSAT

Întrucât nu am găsit pe internet o prezentare a metodei de reducere alese de mine, am să o prezint în cele ce urmează
- În primul pas, combin toate porțile ```AND```/```OR``` imbricate
- Apoi, așa cum am menționat mai sus, parcurg arborele recursiv plecând din rădăcină, în adâncime pentru a avea o abordare Bottom-Up
- Când se ajunge la o variabilă, se încheie parcurgerea ramurii respective; Când se ajunge la o poartă de tip ```AND```, se verifică, pe rând, pentru fiecare copil, dacă este variabilă, caz în care îl las nemodificat, altfel se realizează apelul recursiv pe copilul respectiv. Analog pentru o poartă ```OR```, la care se adaugă verificarea dacă această poartă conține măcar un copil de tip ```AND```, caz în care apelez metoda ```distributeOR```, al cărei scop este să distribuie poarta ```OR``` peste poarta ```AND```, aplicând distributivitatea și astfel obținând o expresie CNF SAT

Distribuirea porților ```OR``` peste porți ```AND``` se realizează conform următorului exemplu :
- Fie expresia ```(a AND b) OR (c AND d)```, având echivalentul matematic ```(a*b) + (c*d)```.
- Expresia logică va deveni, conform distribuitivității, ```(a OR c) AND (a OR d) AND (b OR c) AND (b OR d)```, având echivalentul matematic ```(a+c)*(a+d)*(b+c)*(b+d)```. Desfăcând parantezele, se obține ```(a*a + a*d + a*c + c*d) * (b*b + b*c + b*d + c*d)```, care se poate simplifica la ```(a + a*d + a*c + c*d) * (b + b*c + b*d + c*d)```, iar apoi la ```(a*(1 + c + d) + c*d) * (b*(1 + c + d) + c*d)```, iar apoi la ```(a + c*d) * (b + c*d)```, iar astfel ajungem la formula inițială, ```a*b + a*c*d + b*c*d + c*d*c*d = (a*b) + (c*d)(1 + a + b + c*d) = (a*b) + (c*d)```
- Algoritmic, reducerea pentru input-ul prezentat anterior ar parcurge doar 4 pași pentru a genera combinațiile aferente.
- Pe un caz general, unde să presupunem că avem ```orNr``` porți de tip OR cu ```orVarNr``` variabile și ```andNr``` porți de tip AND ca și copii ai acestora, iar aceste porți au ```andVarNr``` variabile copil și ```orInAndNr``` porți de tip OR.
- Pentru a se realiza reducerea pe un astfel de exemplu, folosind algoritmul descris mai sus, se vor parcurge următorii pași :
  - Se parcurg cele ```orNr``` porți. Pentru una anume se va executa astfel:
    - Se fixează în toate combinațiile copiii de tip variabilă, adică ```orVarNr``` iterații
      - Se realizează combinațiile parcurgând copiii ```AND``` (```andNr``` iterații), iar aici când se vor selecta variabile vor fi ```andVarNr``` iterații, iar când se vor selecta porți ```OR``` vor fi ```orInAndNr``` iterații
  - Se ajunge astfel la un număr de ```orNr * orVarNr * andNr * andVarNr * orInAndNr``` iterații posibile. Unele vor fi excluse în anumite cazuri. Acum, presupunând că avem un caz extrem unde toate valorile nedeterminate anterior sunt un număr suficient de mare ```n```, complexitate algoritmului în timp s-ar găsi în ```O(n^5)```, deci polinomială. Se observă totodată cum complexitate scalează cu dimensiunea input-ului, dar totuși se încadrează în timp poliomial.
- Un aspect important este acela că nu este nevoie să se introducă noi variable, deoarece se pot folosi variabilele deja existente.