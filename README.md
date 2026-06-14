# 📱 FlowWidget

Um esqueleto de rotina inteligente, flutuante e totalmente focado em **privacidade e soberania de dados**. O FlowWidget roda 24/7 direto no seu dispositivo Android, sem conexões externas, sem APIs gringas e sem coletar seus dados na nuvem.

---

## 🎯 Objetivo do Projeto
O FlowWidget nasceu da necessidade de um organizador de rotina que não te prenda a ecossistemas fechados. Ele atua como um "overlay" (camada visual) por cima do sistema operacional, mantendo o bloco de tarefas atual e um timer regressivo sempre visíveis, ajudando no foco e na produtividade em tempo real.

## 🛠️ Funcionalidades Principais

* **Widget Flutuante (Overlay Nativo):** Camada visual móvel que exibe o timer regressivo e as sub-tarefas ativas por cima de qualquer aplicativo.
* **Banco de Dados 100% Local (Room/SQLite):** Seus blocos de estudo, trabalho e vida pessoal nascem e morrem dentro do armazenamento interno do seu celular.
* **Calendário Compacto de Foco Temporal (3x7):** Um grid de 3 semanas (7 colunas) fixado na tela de configurações com efeito de degradê (Fade Out) para dias distantes, mantendo sua atenção no presente imediato (Janela de 7 dias).
* **Lógica do "Momento Livre":** Quando você entra em um bloco de tempo livre, o widget oferece um botão para ocultar a interface, limpando a tela sem interromper o serviço de contagem em background. O widget reaparece sozinho assim que o próximo bloco ativo começa.
* **Gestão Inteligente de Conflitos:** Separação entre rotinas fixas (recorrentes da semana com seleção de múltiplos dias) e pontuais (datas específicas), com sistema de priorização via alertas.
* **Blindagem Standby (Anti-Kill):** Implementação de `START_STICKY`, Watchdog (`BroadcastReceiver`) e remoção de otimização de bateria para garantir que o sistema operacional não encerre o cronômetro durante a noite.

---

## 🏗️ Arquitetura e Tecnologias
* **Linguagem:** Kotlin
* **Engine de Banco de Dados:** Jetpack Room (SQLite local)
* **Concorrência:** Kotlin Coroutines (operações assíncronas assíncronas e timers leves)
* **Interface UI:** Material Design 3 (Dark Mode nativo, cantos arredondados de 24dp/28dp)
* **Background Processing:** Foreground Service com notificação persistente

---

## 🔓 Como Compilar e Rodar (Open Source)

Sendo um projeto de código aberto, você pode clonar e compilar o FlowWidget diretamente na sua máquina:

1. Clone este repositório:
   ```bash
   git clone [https://github.com/SEU_USUARIO/FlowWidget.git](https://github.com/SEU_USUARIO/FlowWidget.git)
