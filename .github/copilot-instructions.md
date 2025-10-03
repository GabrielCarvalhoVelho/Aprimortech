# Copilot Instructions for Aprimortech2

## Visão Geral do Projeto
Este projeto é um aplicativo Android baseado em Kotlin, utilizando Gradle como sistema de build. O código principal está localizado em `app/src/main/` e segue a estrutura padrão de projetos Android modernos.

## Estrutura e Componentes Principais
- **`app/`**: Contém o código-fonte do app, recursos, testes e arquivos de configuração específicos do módulo Android.
- **`build.gradle.kts` e `settings.gradle.kts`**: Configuração global do projeto e dos módulos.
- **`gradle/`**: Scripts e configurações do wrapper do Gradle.
- **`google-services.json`**: Integração com serviços do Firebase.

## Fluxos de Desenvolvimento
- **Build**: Use `./gradlew assembleDebug` para builds de desenvolvimento e `./gradlew assembleRelease` para builds de produção.
- **Testes**: Testes unitários ficam em `app/src/test/` e testes instrumentados em `app/src/androidTest/`. Execute com `./gradlew test` e `./gradlew connectedAndroidTest`.
- **Debug**: Utilize o Android Studio para depuração, breakpoints e inspeção de variáveis.

## Convenções Específicas
- O projeto utiliza Kotlin DSL para scripts Gradle (`*.kts`).
- Siga a estrutura de pacotes e nomes de arquivos conforme o padrão Android.
- Recursos (layouts, strings, drawables) ficam em `app/src/main/res/`.
- Configurações sensíveis (ex: chaves de API) devem ser mantidas fora do controle de versão.

## Integrações e Dependências
- **Firebase**: Configurado via `google-services.json`.
- Dependências são gerenciadas via Gradle Kotlin DSL, consulte `build.gradle.kts` e `app/build.gradle.kts` para versões e plugins.

## Exemplos de Comandos Úteis
- Build debug: `./gradlew assembleDebug`
- Build release: `./gradlew assembleRelease`
- Testes unitários: `./gradlew test`
- Testes instrumentados: `./gradlew connectedAndroidTest`

## Dicas para Agentes de IA
- Priorize a leitura dos arquivos `build.gradle.kts` e da estrutura de `app/src/` para entender dependências e arquitetura.
- Ao criar novos módulos ou recursos, siga a estrutura e convenções já presentes.
- Consulte a documentação do Android e do Gradle Kotlin DSL para padrões não explícitos no projeto.

---
Adapte e expanda estas instruções conforme o projeto evoluir ou padrões específicos forem adotados.