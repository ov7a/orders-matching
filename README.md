# orders-matching

Классическая задача по матчингу заявок. Условие см. в [task.md](task.md)

# Запуск

Можно сделать просто

`sbt run`

или запустить с кастомными именами файлов:

`sbt "run clients.txt orders.txt result.txt"`

# Краткое описание

Основная логика находится в OrdersMatcher (где определяется тип матчинга) и в OrdersProcessor (где идет обработка матча).

# Особенности и что можно улучшить

## Структура данных для хранения текущих заявок

Сейчас это словарь акция -> связный список заявок по ней. 
Технически, это не совсем связный список (т.к. там идет копирование при присоединении хвоста), но на асимптотику существенно это не влияет.
В текущей реализации номер заявке не присваивается, хотя если была бы распределенность, то стоило бы.
Можно сделать дополнительное разделение по типу заявки, но для двух типов это перебор. 
Можно сделать сбалансированное ДДП, где в качестве ключа будет номер заявки - обход будет по прежнему за линейное время, но удаление за логарифмическое (что на общее время существенно не влияет). 
Но вообще СД будет в том числе зависеть от БД, где будут в итоге храниться активные заказы, и от бизнес логики (например, от того, как обрабатывать заявки от одного клиента).

## Валидация заявок

Во-первых, сейчас валидация сделана тупо `true`/`false` вместо нормального статуса валидирования.

Во-вторых, по условию неясно, как обрабатывать ситуацию, когда клиент подряд размещает 2 заявки, обе из которых по отдельности могут быть удовлетворены, а вместе нет. Сейчас производится ленивое удаление - если в списке была встречена невалидная заявка, то она будет удалена. Таким образом, в примере одна заявка будет сматчена, другая - удалена.

В-третьих, из условия неясно, нужно ли удалять заявки, ставшие невалидными (связано с предыдущим). Сейчас - удаляются лениво.

Теоретически, можно сделать логику резервирования ресурсов (т.е. если клиент хочет купить 10 акций по 3, то 30$ у него резервируются), но даже она не может гарантировать обработку всех возможных кейсов.

## Цена определяется по заявке продажи

Т.к. по условию надо было матчить первую по очереди заявку. Никакой логики про лучшую цену, последнюю цену или среднюю цену не реализовано.

## Обработка заявок от одного клиента

Логика обработки заявок от одного клиента по одному типу акции не реализована. Т.е. ни объединения, ни взаимной отмены заявок, ни чего-то другого нет. В некоторых случаях такие заявки отбрасываются (случай @SameClientMatch@), однако все случаи не покрыты: например, если есть если две продажи от клиентов C1 и C2, и приходит покупка от C2, то вместо игнорирования (т.к. уже есть продажа от C2) будет матчинг с продажей C1. 

## Частичное совпадение

Частичное совпадение обрабатывается, но только в том случае, если полностью удовлетворяется хотя бы одна из заявок. Другие случаи не рассмотрены.

## Есть заготовка под другие типы заявок

Может показаться овер-инжинирингом, но с учетом скудности условия и отсутствия обратной связи - почему бы и нет (вариант операции - аренда). 

## Транзакций нет

Под них придется довольно много переделывать. По-хорошему, можно простенько сымитировть их, присобачив, например SQLite.

## Баланс не проверяется повторно при совершении сделки

Ибо это все равно должно быть в транзакции и/или должна быть логика повтора при неудаче.

## Нет логгирования

А по-хорошему должен логгироваться хотя бы сам матчинг.

## Не обрабатываются ошибки чтения/парсинга и записи 

И, соответственно, нет юнит-тестов. Вряд ли кто-то будет всерьез использовать данную логику как консольное приложение. Считается, что все входные данные валидны.

## Нет именованных аргументов командной строки

По той же причине, что и в предущем пункте.

## Юнит-тесты стоит отрефакторить

Во-первых, некоторые хитрые случаи (в т.ч. описанные здесь) они не покрывают - можно написать некорректную программу, которая пройдет тесты.

Во-вторых, многие из них содержат дублирующийся код, хотя некоторым нравится, когда юнит содержит все нужное в себе.

В-третьих, есть торчащие наружу абстракции (например `Map("A" -> Nil)`)
