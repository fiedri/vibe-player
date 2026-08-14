# Skill Registry — vibe-player

Resolved: 2026-08-13 by sdd-init. Project-level skills win over user-level by name.

## Project Skills

| Skill | Path | Trigger |
|-------|------|---------|
| frontend-design | `.agents/skills/frontend-design/SKILL.md` | Building new UI or reshaping existing one; aesthetic direction, typography, avoiding templated defaults |

## User Skills (deduplicated)

| Skill | Path | Trigger |
|-------|------|---------|
| adaptive | `~/.config/opencode/skills/adaptive/SKILL.md` | Making app UI adapt to Android devices (phones, tablets, foldables, TV, Auto, XR); Compose MediaQuery, Navigation3 Scenes, Grid/FlexBox |
| agp-9-upgrade | `~/.config/opencode/skills/agp-9-upgrade/SKILL.md` | Upgrading Android project to AGP 9. NOT for KMP projects |
| android-cli | `~/.config/opencode/skills/android-cli/SKILL.md` | Installing/using `android` CLI; creating projects, running apps, AVDs, SDK components, docs lookup |
| android-intent-security | `~/.config/opencode/skills/android-intent-security/SKILL.md` | Auditing AndroidManifest components or incoming Intent handling for Intent Redirection / unauthorized access |
| appfunctions | `~/.config/opencode/skills/appfunctions/SKILL.md` | Exposing key app workflows to Android system / AI agents; AppFunction Kotlin code, KDoc refinement |
| camerax | `~/.config/opencode/skills/camerax/SKILL.md` | Android camera development with CameraX; async recording lifecycle, low-level interop, Media3 effects |
| caveman-commit | `~/.config/opencode/skills/caveman-commit/SKILL.md` | Ultra-compressed conventional commit messages (subject ≤50 chars) |
| display-glasses-with-jetpack-compose-glimmer | `~/.config/opencode/skills/display-glasses-with-jetpack-compose-glimmer/SKILL.md` | Android XR apps for display glasses with Compose Glimmer UI toolkit |
| edge-to-edge | `~/.config/opencode/skills/edge-to-edge/SKILL.md` | Migrating Compose app to adaptive edge-to-edge; fixing system-bar/IME inset overlap |
| engage-sdk-integration | `~/.config/opencode/skills/engage-sdk-integration/SKILL.md` | Integrating/debugging Play Engage SDK; mapping data classes to entities |
| find-skills | `~/.agents/skills/find-skills/SKILL.md` | User asks "how do I do X" / "find a skill for X"; discovering installable skills |
| go-testing | `~/.config/opencode/skills/go-testing/SKILL.md` | Writing Go tests, teatest, Bubbletea TUI testing |
| migrate-xml-views-to-jetpack-compose | `~/.config/opencode/skills/migrate-xml-views-to-jetpack-compose/SKILL.md` | Migrating Android XML View to Jetpack Compose |
| navigation-3 | `~/.config/opencode/skills/navigation-3/SKILL.md` | Jetpack Navigation 3: install/migrate, deep links, backstacks, scenes, Hilt/ViewModel interop |
| perfetto-sql | `~/.config/opencode/skills/perfetto-sql/SKILL.md` | Translating natural-language data intents into Perfetto SQL; querying local trace files |
| perfetto-trace-analysis | `~/.config/opencode/skills/perfetto-trace-analysis/SKILL.md` | Root-causing latency/memory/jank from Perfetto traces |
| play-billing-library-version-upgrade | `~/.config/opencode/skills/play-billing-library-version-upgrade/SKILL.md` | Upgrading from legacy Play Billing Library to latest stable |
| play-policy-insights | `~/.config/opencode/skills/play-policy-insights/SKILL.md` | Verifying app against Google Play Policy domains; compliance reports |
| r8-analyzer | `~/.config/opencode/skills/r8-analyzer/SKILL.md` | Analyzing R8 keep rules for redundancies / overly broad rules |
| skill-creator | `~/.config/opencode/skills/skill-creator/SKILL.md` | Creating new AI agent skills per Agent Skills spec |
| styles | `~/.config/opencode/skills/styles/SKILL.md` | Integrating Jetpack Compose Styles API; component themes, styleable components |
| supabase-postgres-best-practices | `~/.agents/skills/supabase-postgres-best-practices/SKILL.md` | Writing/reviewing/optimizing Postgres queries, schemas, configs |
| testing-setup | `~/.config/opencode/skills/testing-setup/SKILL.md` | Testing strategy for native Android apps; unit/UI/screenshot/E2E harnesses |
| verified-email | `~/.config/opencode/skills/verified-email/SKILL.md` | OTP-less verified email retrieval via Android Credential Manager API |
| wear-compose-m3 | `~/.config/opencode/skills/wear-compose-m3/SKILL.md` | Wear OS Compose Material3: AppScaffold, TransformingLazyColumn, ambient mode, migrations |

## Excluded

- `sdd-*` phase skills (loaded by orchestrator directly)
- `_shared` convention docs
- `skill-registry` (this registry's generator)

## Notes

- Mirror copies of the same skills exist under `~/.gemini/skills/`, `~/.copilot/skills/`, `~/.agents/skills/` — deduplicated by name, resolved to `~/.config/opencode/skills/` (or `~/.agents/skills/` for find-skills and supabase-postgres-best-practices, which only exist there).
- `skills-lock.json` in repo root pins `frontend-design` source (anthropics/skills, github).
- No project-level convention files (AGENTS.md/CLAUDE.md/etc.) found in repo root.