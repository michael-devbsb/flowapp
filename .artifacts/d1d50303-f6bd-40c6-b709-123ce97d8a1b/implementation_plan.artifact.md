# Plano de Modernização do FlowWidget

Este plano visa concluir a migração do projeto para tecnologias modernas (Jetpack Compose), organizar a arquitetura do projeto seguindo padrões Clean/MVVM, otimizar o consumo de recursos do serviço de monitoramento e garantir compatibilidade total com o Android 15+ (SDK 36).

## User Review Required

> [!IMPORTANT]
> **Migração Destrutiva do Banco de Dados:** Como o projeto já possui `fallbackToDestructiveMigration()`, as mudanças no banco de dados podem apagar dados locais de teste se a versão for incrementada sem migração formal. Se houver dados reais, precisaremos criar uma migration.
> **Permissões de Alarme Exato:** Para o SDK 36, o uso de `SCHEDULE_EXACT_ALARM` é restrito. Manteremos a lógica atual, mas o sistema pode exigir que o usuário habilite manualmente nas configurações.

## Proposed Changes

### 1. Reorganização de Arquitetura [Componente: Core/Data]
Moveremos os arquivos para pacotes lógicos para melhorar a manutenibilidade.

#### [MODIFY] [RoutineDao.kt](file:///home/michaelmonteiro/AndroidStudioProjects/FlowWidget/app/src/main/java/com/example/flowwidget/RoutineDao.kt)
- Alterar `getActiveBlock` para retornar `Flow<RoutineBlock?>`.

#### [MODIFY] [AppDatabase.kt](file:///home/michaelmonteiro/AndroidStudioProjects/FlowWidget/app/src/main/java/com/example/flowwidget/AppDatabase.kt)
- Mover para `com.example.flowwidget.data.local`.

---

### 2. Otimização do Serviço [Componente: Service]
Substituir o loop de polling por observação reativa de dados.

#### [MODIFY] [FloatingWidgetService.kt](file:///home/michaelmonteiro/AndroidStudioProjects/FlowWidget/app/src/main/java/com/example/flowwidget/FloatingWidgetService.kt)
- Remover o loop `while(isActive)` com `delay(1000)`.
- Implementar a coleta do `Flow` do repositório para atualizar a notificação em tempo real apenas quando os dados mudarem.

---

### 3. Migração para Jetpack Compose [Componente: UI]
Substituição completa dos layouts XML e Activities baseadas em View por componentes declarativos.

#### [MODIFY] [MainActivity.kt](file:///home/michaelmonteiro/AndroidStudioProjects/FlowWidget/app/src/main/java/com/example/flowwidget/MainActivity.kt)
- Implementar `enableEdgeToEdge()`.
- Criar a UI em Compose: Botão de toggle estilizado e botão de acesso às configurações.

#### [MODIFY] [SettingsActivity.kt](file:///home/michaelmonteiro/AndroidStudioProjects/FlowWidget/app/src/main/java/com/example/flowwidget/SettingsActivity.kt)
- Implementar `enableEdgeToEdge()`.
- Reimplementar o Calendário Horizontal e a Lista de Rotinas em Compose utilizando `LazyRow` e `LazyColumn`.

#### [NEW] `com.example.flowwidget.ui.components`
- [NEW] `CalendarComponent.kt`: UI do seletor de datas.
- [NEW] `RoutineItem.kt`: Card individual da rotina.
- [NEW] `RoutineBottomSheet.kt`: Versão Compose do formulário de criação/edição.

#### [DELETE] Layouts XML
- Remover `activity_main.xml`, `activity_settings.xml`, `item_routine.xml`, etc., após a migração bem-sucedida.

---

### 4. Polimento e SDK 36 [Componente: System]
Ajustes para garantir que o app utilize toda a tela e respeite as diretrizes do Android 15.

#### [MODIFY] [themes.xml](file:///home/michaelmonteiro/AndroidStudioProjects/FlowWidget/app/src/main/res/values/themes.xml)
- Simplificar o tema para delegar o controle de cores ao Compose `MaterialTheme`.

## Verification Plan

### Automated Tests
- Executar `gradle assembleDebug` para garantir que a refatoração de pacotes não quebrou as referências do Hilt/KSP.

### Manual Verification
1. **Lançamento do Serviço:** Verificar se o botão na `MainActivity` ativa o Foreground Service corretamente.
2. **Atualização Reativa:** Adicionar uma tarefa que comece no minuto seguinte e verificar se a notificação muda automaticamente sem precisar reiniciar o app.
3. **Interface Adaptativa:** Testar a `SettingsActivity` com navegação por gestos habilitada para garantir que nenhum elemento UI fique sob a barra de navegação (Edge-to-Edge).
