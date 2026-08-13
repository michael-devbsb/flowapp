# 📱 FlowWidget

Um esqueleto de rotina inteligente, flutuante e totalmente focado em **privacidade e soberania de dados**. O FlowWidget roda 24/7 direto no seu dispositivo Android, sem conexões externas, sem APIs gringas e sem coletar seus dados na nuvem.

---

## 🎯 Objetivo do Projeto
O FlowWidget nasceu da necessidade de um organizador de rotina que não te prenda a ecossistemas fechados. Ele atua como um "overlay" (camada visual) por cima do sistema operacional, mantendo o bloco de tarefas atual e um timer regressivo sempre visíveis, ajudando no foco e na produtividade em tempo real.

## 🛠️ Funcionalidades Principais

* **Widget Flutuante (Overlay Nativo):** Camada visual móvel com largura fixa (1/4 da tela) que exibe o timer regressivo e as sub-tarefas ativas por cima de qualquer aplicativo.
* **Lembretes Antecipados (15 min):** Sistema de notificações inteligente que avisa o usuário 15 minutos antes do início de qualquer tarefa agendada.
* **Banco de Dados 100% Local (Room/SQLite):** Seus blocos de estudo, trabalho e vida pessoal nascem e morrem dentro do armazenamento interno do seu celular.
* **Calendário Vertical Dinâmico:** Visualização infinita de semanas com scroll vertical, mantendo sempre 3 linhas visíveis. O cabeçalho do mês atualiza-se automaticamente conforme o scroll.
* **Seletor de Cores Arco-Íris:** Personalização de blocos com uma paleta fixa de 12 cores vibrantes selecionáveis via interface intuitiva.
* **Detalhes da Rotina:** Visualização completa de informações do bloco (horário, dias, tarefas) através de um modal centralizado, sem precisar entrar no modo de edição.
* **Lógica do "Momento Livre":** Quando você entra em um bloco de tempo livre, o widget oferece um botão para ocultar a interface. O widget reaparece sozinho assim que o próximo bloco ativo começa.
* **Gestão Inteligente de Conflitos:** Separação entre rotinas fixas (recorrentes da semana) e pontuais, com sistema de priorização via alertas.
* **Blindagem Standby (Anti-Kill):** Implementação de `START_STICKY`, Watchdog (`BroadcastReceiver`) e `AlarmManager` para garantir que o sistema não encerre o cronômetro ou os lembretes.

---

## 🏗️ Arquitetura e Tecnologias
* **Linguagem:** Kotlin
* **Engine de Banco de Dados:** Jetpack Room (SQLite local)
* **Concorrência:** Kotlin Coroutines (operações assíncronas e timers leves)
* **Interface UI:** Material Design 3 (Dark Mode nativo, cantos arredondados de 24dp/28dp)
* **Background Processing:** Foreground Service com notificação persistente

---

## 🔓 Como Compilar e Rodar (Open Source)

Sendo um projeto de código aberto, você pode clonar e compilar o FlowWidget diretamente na sua máquina:

1. Clone este repositório:
   ```bash
   git clone https://github.com/michael-devbsb/FlowWidget.git
   ```

2. Abra o projeto no **Android Studio** (versão Ladybug ou superior recomendada).
3. Certifique-se de que o arquivo `gradle.properties` contenha a linha para suporte ao Room/KSP:
   ```properties
   android.disallowKotlinSourceSets=false
   ```

4. Conecte seu dispositivo Android via USB (com a Depuração USB ativa).
5. Certifique-se de conceder as seguintes permissões ao rodar o app:
   * **Sobreposição a outros aplicativos** (essencial para a janela flutuante).
   * **Ignorar otimizações de bateria** (para evitar o encerramento em standby).

---

## 📄 Licença

Este projeto é distribuído sob a licença **MIT** — veja o arquivo [LICENSE](LICENSE) para mais detalhes. Sinta-se livre para clonar, modificar e usar no seu próprio ecossistema pessoal.
