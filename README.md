# Pomodroid
Um simples Pomodoro criado Nativamente para Android com Kotlin.

## Peculiaridades
Acabei lendo alguns artigos relacionados aos services que podem ser criados juntamente com a aplicação, uma delas era o Service no qual poderia rodar em Background mesmo com o aplicativo fechado desde que tenha uma notificação "On Going".
Com isso decidi criar esse pomodoro assim, independente da aplicação ser fechada ele continuará rodando.

## Conhecimentos utilizado
- Arquitetura MVVM
- Services / Foreground Service
- Intent
- PendingIntent
- LiveData
- StateFlow
- Dagger Hilt
- NotificationManager and NotificationBuilder
- MediaPlayer
- Versions Catalog

## Todo List
- [x] Service Pronto <br>
- [x] Binding Service <br>
- [x] Atualizar Service por Intents <br>
- [x] Atualizar notificação <br>
- [x] Interface observando mudança do Service <br>
- [x] Lógica do alarme <br>
- [x] Chips contendo "Pomodoro", "Short Break" e "Long Break" funcional <br>
- [ ] Alterar Activity para Fragment
- [ ] Utilizar ViewModel para observar o service (Se for possivel)
- [ ] Tela de Configurações
- [ ] Configuração dos Chips
- [ ] Configuração do Ringtone
- [ ] Alterar tema do aplicativo
