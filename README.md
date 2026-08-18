# 📱 Flow

Um organizador de rotina inteligente e focado em **privacidade e soberania de dados**. O Flow funciona de forma totalmente local no seu dispositivo Android, sem depender de nuvem ou APIs externas, garantindo que seus dados permaneçam privados.

---

## 🎯 Objetivo do Projeto
O Flow ajuda você a manter o foco através de uma notificação persistente que exibe a tarefa atual, sub-tarefas e um cronômetro regressivo direto na central de notificações. Ele é ideal para quem busca produtividade sem distrações e com total controle sobre suas informações.

## 🛠️ Funcionalidades Principais

*   **Navegação Fluida:** Transição entre dias através de **scroll horizontal** na lista de blocos, sincronizado com o calendário.
*   **Monitoramento Ativo:** Notificação persistente com timer em tempo real e lista de tarefas do bloco atual.
*   **Gestão de Rotina:** Suporte para blocos de tempo **Fixos** (semanais com indicadores minimalistas) e **Pontuais** (datas específicas).
*   **Controle de Conclusão:** Marque tarefas pontuais como concluídas diretamente na lista com feedback visual (tachado e alteração de contraste).
*   **Calendário Inteligente:** Grid de calendário personalizado para navegação rápida entre dias e meses.
*   **Interface Moderna (Material 3):** Experiência visual limpa com tema adaptativo e foco em usabilidade.
*   **Modo Escuro & Claro Dinâmico:** Alternância instantânea entre temas com ajuste automático da interface.
*   **Backup e Restauração:** Sistema de exportação e importação via **CSV robusto** (com suporte a caracteres especiais e aspas), permitindo migrar ou restaurar sua rotina facilmente.
*   **Visão Geral:** Acesso rápido a todos os blocos cadastrados através de um botão central em formato de pílula.
*   **Resiliência:** Implementação de `Foreground Service` e `AlarmManager` para garantir que lembretes e o monitoramento funcionem perfeitamente.

---

## 🏗️ Arquitetura e Tecnologias
*   **Linguagem:** Kotlin
*   **UI:** Jetpack Compose (Material Design 3)
*   **Persistência:** Room Database (SQLite local)
*   **Injeção de Dependências:** Hilt (Dagger)
*   **Navegação & Estado:** ViewModel, Flow e Navigation Compose.
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
   * Certifique-se de marcar as assinaturas **V1 e V2** para máxima compatibilidade.

---

## 📄 Licença

Este projeto é distribuído sob a licença **MIT**. Sinta-se à vontade para usar e modificar para seu uso pessoal.
