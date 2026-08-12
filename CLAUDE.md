<!-- GENERATED FILE - synced from lux-vault/claude/. Do not edit here: edit lux-vault/claude/ and re-run sync. -->

# LUX shared context (all Zero/LUX repos)

LUX Energy Twin: an open-source, agent-based AnyLogic model of energy systems, focused on electricity and grid congestion. Power (kW) balance only — no current/voltage; congestion only at grid nodes, never cables; 15-min timesteps; ≤ 1 year simulated. Currently AnyLogic 8.9.9 / Java 17, but the models track each new AnyLogic release (Java follows AnyLogic) — read the `.alpx` header rather than trusting a version quoted anywhere, including docs.lux.energy, which lags. Public docs: https://docs.lux.energy/

## The four packages

| Package | Repo | Role |
|---|---|---|
| Engine | zero_engine | Calculation model. Main agent `EnergyModel` owns all populations and accumulated flows |
| Loader/Interface | zero_Interface-Loader | Library: loads data into engine populations; all interface visuals |
| ResultsUI | zero_results_UI | Chart/KPI presentation, configured by the interface |
| Project | private, from LUX_ProjectTemplate | Child loader + child interface + startUp (main); project data; overrides |

## The golden rule (layering)

The three core repos are **generic public libraries** — no project names, no project data, no single-project functions, ever. Project-specific work belongs in the project repo via the child loader/interface. Order of preference for any change: **project repo → loader override surface → engine/resultsUI**. Core changes require the impact check in `../lux-vault/lux-vault/10-architecture/layering-and-override-rules.md`, and land via PR: reviewers check semantics/implementation *and* run the branch against existing custom models. Loader function signatures are load-bearing: breaking one breaks every customer project.

**Known exception — charts.** ResultsUI charts have no project-level override. Adding or changing a chart is always a `zero_results_UI` branch + PR, even when a single project drove the request; only chart *selection* is project configuration.

## AnyLogic files — safety essentials

Model source is XML with embedded Java (split `.alpx` + `_alp/` in core repos and new projects; monolithic `.alp` in older ones). Before editing any of it, use the `alp-editing` skill. Behavior lives in plain functions — AnyLogic events are avoided and statecharts unused, so don't reach for them when adding logic. Non-negotiables: never change `<Id>` values; never hand-edit `database/`, `cache/`, `*.bak`, jars; keep XML well-formed and CDATA intact; new files/jars must be declared in `_alp/ModelResources.xml`; structural additions (new agents/parameters/functions) happen in the AnyLogic IDE, not by hand-writing XML. Plain Java in `_alp/Classes/Class.*.java` and `Code/*.java` is safe to edit as normal code.

## Knowledge vault (../lux-vault)

Deep knowledge lives in the sibling checkout `../lux-vault/lux-vault/` (private Obsidian vault). Sections: `10-architecture` (packages, layering, data flow, file formats), `20-engine`/`21-loader-interface`/`22-results-ui`/`23-project-layer` (module notes), `30-conventions` (naming/semantics — prefixes like `GC*`, `J_*`, `I_*`, `OL_*`, `p_*`/`v_*` are load-bearing), `40-playbooks`, `50-decisions` (ADRs), `60-learnings`.

Protocol:
- Consult before architectural decisions, cross-package changes, or unfamiliar modules: Grep the vault for the module/topic, read the matching notes.
- **Debugging a symptom?** Start at `40-playbooks/debugging.md` — a symptom → likely-cause index. The rest of the vault is indexed by module, which is the wrong entry point when you don't yet know where the fault is.
- Trust order: note `status: verified` > `unverified` > guessing — and **code beats notes**. If code contradicts a note, that's a learning: capture it.
- If the vault is missing (external contributor), proceed with this file + repo READMEs + https://docs.lux.energy/.

## Verification reality

No headless build exists for the models: AnyLogic compiles on project reload, so the final gate is opening/running in the IDE (or AnyLogic Cloud for rapid-runs). What you *can* do after edits: XML well-formedness checks, reviewing the diff for forbidden changes (`<Id>`, database/), and consistency greps. The loader has a JUnit-in-AnyLogic test model (`tests/Zero_Loader_Test.alpx`) — runs inside AnyLogic, not via Gradle. State honestly what was and wasn't verified.

## Session habits

- **Check the Ignore flag before trusting model code.** AnyLogic elements carry `<ExcludeFromBuild>true</ExcludeFromBuild>` when ignored — functions, variables, even whole agent types. Ignored elements don't compile, aren't part of the architecture, and must not be used to infer behavior or be documented in the vault. Superseded logic is routinely left in place this way rather than deleted, so a plausible-looking function may be dead.
- **Match surrounding code; don't normalize.** No formatter config, no agreed brace/indent style, no nullability policy; `p_*`/`v_*` prefixes are habits, not rules. Reformatting, adding `Optional`, or renaming members to fit a convention creates unreviewable diffs against a codebase with no standard to converge on.
- Before committing non-trivial changes: run the `lux-reviewer` agent on the diff.
- Consequential design decision made? Record it: `/adr`.
- End of a session that produced durable insight (surprise, contradiction, gotcha, failed approach): offer `/learn` — it PRs the insight into the vault. This loop is how the whole setup improves; don't skip it.

# This repo: zero_Interface-Loader

Library repo, extended by every customer project's child loader/interface. The most contract-sensitive repo of the four.

## Structure

- `Zero_Interface-Loader.alpx` + `_alp/` — split format
- `_alp/Agents/` — `Zero_Loader` (data → engine populations), `Zero_Interface` (GIS map, sliders, scenario buttons, legend), `UI_*` and `tab*` agents (interface tabs)
- `_alp/Classes/` — Lombok `*_data` record classes (one Excel sheet ↔ one class: `Building_data`, `Cable_data`, `GridNode_data`, ...), DSO tariffs (`J_GridOperatorTariffs<DSO>_<year>`), scenarios (`J_scenario_*`), utilities
- `tests/` — `Zero_Loader_Test.alpx` (JUnit-in-AnyLogic test model) + Gradle project that only builds the JUnit fat-jar for AnyLogic import
- `lombok.jar` — required; AnyLogic setup per https://docs.lux.energy/dev/loader/lombok/lombok/

## Hard rules (from the repo README — these are policy)

- No project names, no non-generic data, no single-project functions, no project images. Ever.
- This repo ships default load-in structures for project types `BUSINESSPARK` and `RESIDENTIAL`. Projects that deviate override functions in **their child loader** — supporting a deviation never means adding it here, unless it's genuinely generic (then it's a Workflow-A feature with an ADR).
- **Function signatures and collection names are the override surface.** Changing one breaks child loaders in every customer project. Additive evolution strongly preferred; breaking changes need an ADR + coordinated migration.

## Before you commit here

1. Would an existing child loader still compile against this? 2. Do `*_data` classes still match their Excel column expectations? 3. Interface still configures resultsUI correctly? 4. Nothing project-specific leaked in? Run `lux-reviewer`.
