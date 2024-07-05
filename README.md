# Szallasok

## Felhasználókezelés
A felhasználó regisztrálhat ha még nincs fiókja. 2 típusú felhasználó is van, az egyik a bérlő a másik pedig az eladó. A kötelező adatok megadása után már bejelentkezhet (ha már regisztrált de elfelejtette a jelszavát akkor emailes emlékeztetőt kérhet), ahol áttekintheti a saját adatait, módosíthatja azokat.

## Admin
Van egy admin felhasználó (email: admin | jelszó: admin) aki az összes regisztrált felhasználót és azok adatait megtekintheti illetve töröltheti is őket és az összes hozzájuk tartozó adatot is a rendszerből (pl a bérlőnek az összes olyan foglalását ami később van mint a törlés dátuma, kiadónak pedig az összes szállását ezzel) és ezekről kapnak mind kapnak emailes tájékoztatót az érintett felhasználók.
Továbbá megtekintheti a szállásokra leadott értékeléseket is és törölheti is azokat illetve módosíthatja saját adatait is ha szeretné.



## A projektről
Ebben a spring alkalmazásban lehetőség van áttekinteni az összes szállást és hozzá tartozó értékeléseket. Ha egy bérlő be van jelentkezve esetleg akkor lehetősége nyílik értékelést írni a szállásról, illetve lefoglalni azt. A foglalásnál meg kell adni azt, hogy mettől meddig szeretné lefoglalni az adott szállást és hány főre foglalna. Abban az esetben, hogyha az adott időpontra nincs elegendő hely a megadott főnek, akkor természetesen nem engedi lefoglalni és javasol a megadott időintervallumhoz legközelebb eső új időpontot ugyanennyi napra, amikor már elférne mindenki oly módon, hogy mutat egy előbbi dátumot de csak akkor, hogyha ez az új időintervallum még nem esne bele a mai napba vagy előbbre, illetve mutat egy kívánt időintervallum utáni lehetőséget is és ezt a foglalást még módosíthatja, illetve törölheti is a bérlő abban az esetben, hogyha még nem a foglalás idején tenné meg ezeket.
Ha lefoglalnak egy szállást, módosítják vagy törlik a foglalást akkor arról mindig kap emailt az érintett kiadó.

A kiadó új szállásokat hozhat létre, tölthet fel minden szállásához képet is hogyha szeretne. Abban az esetben, hogyha a szállást módosítani vagy törölné a kiadó, akkor az összes érintett bérlő emailes tájékoztatést kap az esetről.

### Egyéb tudnivalók
Az alkalmazás spring keretrendszerben készűlt java nyelven maven és thymeleaf segítségével MySQL adatbázissal illetve futtatható Docker környezetben(mysql image megléte szükséges hozzá) majd a docker-compose up --build parancs kiadása után a localhost:8090 url-en el is indul az alkalmazás.
Meg vannak benne valósítva a CRUD műveletek a megfelelő hibakezelésekkel együtt valamint a JDBC alkalmazása mellett sql lekérdezésekkel is dolgoztam a projekten belül (pl a foglalás menete).
