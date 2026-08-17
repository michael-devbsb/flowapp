# 📱 Flow

Um organizador de rotina inteligente e focado em **privacidade e soberania de dados**. O Flow funciona de forma totalmente local no seu dispositivo Android, sem depender de nuvem ou APIs externas, garantindo que seus dados permaneçam privados.

---

## 🎯 Objetivo do Projeto
O Flow ajuda você a manter o foco através de uma notificação persistente que exibe a tarefa atual, sub-tarefas e um cronômetro regressivo direto na central de notificações. Ele é ideal para quem busca produtividade sem distrações e com total controle sobre suas informações.

## 🛠️ Funcionalidades Principais

*   **Monitoramento Ativo:** Notificação persistente com timer em tempo real e lista de tarefas do bloco atual.
*   **Gestão de Rotina:** Suporte para blocos de tempo **Fixos** (semanais) e **Pontuais** (datas específicas).
*   **Calendário Inteligente:** Grid de calendário personalizado para navegação rápida entre dias e meses.
*   **Interface Moderna (Material 3):** Experiência visual limpa com tema totalmente neutro (grayscale) para evitar fadiga visual.
*   **Modo Escuro & Claro Dinâmico:** Alternância instantânea entre temas com ajuste automático da barra de status do sistema para visibilidade total.
*   **Seletores Nativos Compose:** Uso de `TimePicker` e `DatePicker` modernos, totalmente integrados ao tema do app.
*   **Salvamento Automático:** Configurações de lembretes salvas instantaneamente com feedback visual (indicador de status no campo).
*   **Exportação CSV:** Exporte toda a sua rotina para um arquivo local para backup ou análise externa.
*   **Resiliência:** Implementação de `Foreground Service` e `AlarmManager` para garantir que lembretes funcionem mesmo em standby.

---

## 🏗️ Arquitetura e Tecnologias
*   **Linguagem:** Kotlin
*   **UI:** Jetpack Compose (Material Design 3)
*   **Persistência:** Room Database (SQLite local)
*   **Injeção de Dependências:** Hilt (Dagger)
*   **Processamento:** Kotlin Coroutines & Flow
*   **Background:** Foreground Service & AlarmManager

---

## 🔓 Como Compilar e Rodar

### Requisitos Mínimos
*   **Android 10** (API 29) ou superior.
*   **Android Studio** Ladybug (ou mais recente).

### Passos para Build
1. Clone o repositório:
   ```bash
   git clone https://github.com/michael-devbsb/FlowWidget.git
   ```
2. Abra no Android Studio.
3. Sincronize o Gradle.
4. Para gerar o APK de teste (Debug):
   * `Build` -> `Build Bundle(s) / APK(s)` -> `Build APK(s)`.
5. Para gerar o APK Final (Release):
   * `Build` -> `Generate Signed Bundle / APK...`
   * Utilize a chave existente no projeto (`flowwidget-key.jks`) se disponível.

---

## 📄 Licença

Este projeto é distribuído sob a licença **MIT**. Sinta-se à vontade para usar e modificar para seu uso pessoal.
