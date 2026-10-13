LiftHappens Product Requirements
Owner Pawan Jheeta | Version 0.2 | October 12 2026 | Platform Android
Purpose and product direction
LiftHappens helps a gym user select a workout or individual exercise, record performed sets quickly, follow rest periods, and compare results with previous sessions. It also keeps body weight, personal nutrition targets, and saved meal plans accessible. The first release centers on a reliable offline workout session.
This editable specification proposes implementation behavior and release priorities. Requirements marked User come from Pawan’s supplied draft or instructions. Reference identifies a FitNotes documented feature. Proposal identifies a LiftHappens design decision to review. All release assignments remain editable.
Problem and primary user
The primary user wants the convenience of FitNotes with personal control over exercises, workout uploads, progression rules, and meal plans. Logging must work during a busy gym visit, including when paired exercises must be performed separately.
Success criteria for the first usable release
A user can open a saved workout, record and correct sets, lock and reopen the phone, and recover every confirmed entry. Previous exercise results remain available. A configured rest timer remains accurate after backgrounding. A complete backup can be restored without losing relationships or units.
Product vocabulary
Term	Meaning
Exercise	Reusable movement definition with category and recording fields.
Workout plan	Reusable ordered exercise prescription; planned targets are not completed results.
Routine	Optional collection of workout plans or named training days.
Session	Actual workout on a selected date, with its performed sets.
Set	One performed or planned entry, with completion state and typed values.

 Release scope and navigation
Release	Proposed scope
MVP	Home, exercise library, workout editing, session logging, timer, previous results, calendar history, units, backup and restore.
Next	5×10 suggestions, basic progress charts, body weight, dated meal plans, structured workout import.
Later	Advanced records and calculators, custom body measurements, circuits, automatic cloud backup, FitNotes migration.
Not planned for the first release: accounts, social sharing, AI coaching, barcode nutrition databases, wearable integration, and cross-device synchronization. Meal-plan browsing is a user requirement assigned to Next; move it into MVP if needed.
FN001 Startup and home
Priority: MVP | Basis: User
Show the actual local date and time, top search field, and four primary actions: Exercises, Workouts, History, Meal Plans. Add an unobtrusive Settings entry. The dark charcoal and lime mockup is the proposed visual direction.
Acceptance: Date and time reflect the device; all four actions open their corresponding screen. An unfinished module has a clearly labeled empty state.
FN002 Search
Priority: MVP | Basis: User and Proposal
Search exercise names, category names, workout names, and session dates. Group matches by result type. Accept the date format displayed in the app; provide a date picker alternative. Meal-plan search is Next.
Acceptance: A partial exercise name returns matching entries; selecting a date opens that day’s history; no results produces a helpful empty state.
UX001 Gym usability
Priority: MVP | Basis: Proposal
Use readable high-contrast text, large touch targets, numeric entry controls, explicit units, and clear saved-state feedback. Keep the keyboard from covering Save. Preserve state on rotation and navigation.
Acceptance: The logging flow is usable on the S24 Ultra, with enlarged system text and the keyboard open. A saved set needs no repeated confirmation.

 Exercise library
EX001 Exercise definition
Priority: MVP | Basis: User and Reference S1
Store name, description or notes, category, optional YouTube URL, recording type, default unit, load convention, optional rest override, increment, and origin flag. Required fields are name and category; recording type has an explicit default. Description is optional unless changed by the owner.
Acceptance: Invalid or blank required fields block Save. A valid external URL opens only when tapped. Duplicate names prompt the user to distinguish or cancel.
EX002 Browse and categories
Priority: MVP | Basis: User and Reference S1
Browse alphabetically within categories. The supplied draft uses Abs, Back, Biceps, Cardio, Chest, Legs, Shoulders, Triceps. FitNotes screenshots also show Core and Stretches. Proposed decision: use Core instead of Abs and add Stretches; owner approval is pending.
Acceptance: Every active exercise appears in one primary category. The default exercise catalog is separately curated; this PRD does not claim a complete FitNotes seed list.
EX003 Add edit and archive
Priority: MVP | Basis: User and Proposal
Flag built-in versus custom exercises. Allow custom additions and edits. Archive custom exercises rather than deleting referenced history. Built-in entries may be hidden; permanent deletion policy remains an owner decision.
Acceptance: An archived exercise disappears from new selections but remains readable in previous sessions and existing plan snapshots.
EX004 Recording types and units
Priority: MVP | Basis: Reference S1 and User
Support weight and reps, distance and time, reps only, and time only. Store units with every result. Distinguish total barbell, per-dumbbell, added landmine, machine, and bodyweight loads. Converting display units must convert numeric values, not relabel them.
Acceptance: 15 lb per dumbbell is displayed as such; a timer exercise accepts duration; a unit switch preserves equivalent load and original history.

 Workout planning and sessions
WO001 Create and edit plans
Priority: MVP | Basis: User and Reference S2
Create named plans with ordered exercises, target sets and rep ranges, optional target load, rest, notes, and optional A1/A2 labels. Copy or archive plans. A routine can group named days in a later release.
Acceptance: Editing a plan affects future sessions only. Existing session prescriptions retain their original values.
WO002 Start and resume
Priority: MVP | Basis: Proposal
Start a session from a plan or an individual exercise. Copy prescriptions into session snapshots; show actual entries separately. Store start time, selected local workout date, and status active, completed, or abandoned. Propose one active session at a time.
Acceptance: Reopening an active session restores saved sets and exercise position. Starting another offers resume or finish rather than silently overwriting.
LG001 Save and correct sets
Priority: MVP | Basis: Reference S3 and Proposal
Record actual values, completion state, optional warmup flag, and notes. Save every confirmed entry immediately. Edit, reorder, or remove entries with undo where feasible. Reject negative loads, nonpositive completed reps, and invalid durations. Unfinished placeholders do not count as performance.
Acceptance: A confirmed bench set survives process termination. Editing it updates history. A plan with five targets and only three completed sets remains incomplete.
LG002 Previous results and flexible order
Priority: MVP | Basis: User and Reference S3
Show the latest earlier session for the same exercise with date, units, load convention, and set results. Let the user jump or reorder exercises. Allow paired exercises to be completed as straight sets for a crowded gym.
Acceptance: A1/A2 labels do not force alternation. A new exercise shows No previous results. The current session is excluded from its own previous-session reference.
LG003 Complete a session
Priority: MVP | Basis: Proposal
Display performed results and incomplete targets before finishing. Permit an explicitly confirmed partial session. Preserve omitted targets as omitted, never as completed zeros.
Acceptance: A partially completed workout is saved accurately and can be distinguished from a fully completed prescription.

 Rest and progression
TM001 Rest timer
Priority: MVP | Basis: Reference S4 and User
Resolve rest duration from session override, plan prescription, exercise default, then global default. Provide manual start, optional auto-start on completed set, pause, resume, reset, and cancel. Maintain one active timer. Offer sound and vibration preferences.
Acceptance: Saving an incomplete target does not start rest. A completed bench set can start 120 seconds. Starting a replacement timer visibly replaces the prior one.
TM002 Background and lock behavior
Priority: MVP | Basis: Proposal
Persist the deadline or paused duration and restore remaining time from timestamps rather than counting UI ticks. Define OS notification behavior during implementation. Denied notification permission must not block logging or foreground timing.
Acceptance: Backgrounding for 30 seconds reduces remaining time by about 30 seconds. Rotation does not restart timing. On return after expiry, show Finished; test device sleep and permission denial.
PG001 Exercise specific progression
Priority: Next | Basis: User and Proposal
For the configured 5×10 rule, suggest an increase only after five completed working sets at the same load each meet 10 reps, with user confirmation of acceptable form. Use the configured increment and load convention. Warmup sets, mixed loads, omitted sets, or a mere total of 50 do not qualify.
Acceptance: 10/10/10/5/6 retains the load. Five clean tens offer an increase that the user accepts or declines; historical results are never changed.
PG002 Rule customization
Priority: Later | Basis: Proposal
Support other set counts, rep targets or ranges, configurable increments, and disabled progression. Suggestions are optional product logic rather than a universal training rule.
Acceptance: An exercise without a configured progression rule produces no automatic load recommendation.
Personal seed examples for review
Bench: 5×10; 120-second rest for the next session; baseline 35 lb per side plus bar, 10/10/10/5/6. Bar weight must be confirmed before calculating total load. Chest-supported row: completed 5×10 with 15 lb per dumbbell; next intended load 17.5 lb per dumbbell. The row rest duration is unconfirmed. These are sample configuration and history, not dates to invent or mandatory public seed data.

 History progress and body tracking
HI001 Calendar and session details
Priority: MVP | Basis: User and Reference S5
Open a monthly calendar with dates containing recorded activity highlighted. Select a date to view sessions, exercises, completed sets, and notes. Provide an exercise history entry from its library details.
Acceptance: A logged workout appears on the selected local date. Empty dates remain navigable. Editing a historical set updates its details without changing the workout date.
AN001 Progress charts
Priority: Next | Basis: Reference S6 and Proposal
Provide maximum load, total completed reps, and comparable exercise volume by session. Define normalized volume as load times reps, using an explicit two-dumbbell factor where appropriate; do not include the bar in landmine added-load volume or compare different load conventions as equivalent.
Acceptance: Charts label metric and unit, handle no data, and update after correction. Time and distance records use their own metrics.
AN002 Records and calculators
Priority: Later | Basis: Reference S4 and S6
Offer actual rep records, clearly labeled estimated 1RM, percentage calculations, and a configurable plate calculator. Formula, rep validity range, bar weights, available plates, and rounding policy must be selected before implementation.
Acceptance: An estimate is never displayed as a measured maximum. Plate output sums to the requested total or explains the nearest achievable load.
BW001 Body weight
Priority: Next | Basis: User and Reference S7
Log weight with date, unit, optional note, editable history, goal, and trend. Reach it through History with a Body tab and a secondary home shortcut, preserving the four main actions. Personal seed values are optional: 234.7 lb and 180 lb goal; neither is a new measurement.
Acceptance: Conversions preserve equivalent values. An empty history shows an entry action. The chart distinguishes observations from the goal.
BW002 Other measurements
Priority: Later | Basis: Reference S7
Allow configurable measurement definitions, units, entries, and charts. Disabling a measurement preserves existing entries.
Acceptance: Re-enabling a measurement restores its previous history.

 Meal plans imports and data protection
MP001 Saved meal plans
Priority: Next | Basis: User and Proposal
Create dated plans with name, meals, foods, portions, notes, optional calorie and protein values, and optional shopping items. Sort newest plan dates first. Plan entries describe intended meals; actual consumption tracking is a separate deferred feature.
Acceptance: A plan remains available offline after restart. Planned totals are labeled estimates and appear only when supporting values exist.
MP002 Personal targets and preferences
Priority: Next | Basis: User and Proposal
Allow editable daily calorie and protein targets plus food exclusions. Proposed personal setup: 2,000 calories, 140–165 g protein, no fish, no avocado or cashews, and Bavarian yogurt or desi dahi instead of Greek yogurt. These are editable user preferences, not app-generated prescriptions.
Acceptance: The user can change targets. An explicitly named excluded ingredient in an imported plan is flagged; missing ingredient information is marked unknown rather than safe.
IM001 Workout upload
Priority: Next | Basis: User and Proposal
Define a versioned structured JSON format for plans and exercises. Preview imported fields, flag missing units or unknown exercises, resolve duplicates, and confirm before saving atomically. Word or PDF extraction is deferred. FitNotes migration needs a real sample and a verified mapping.
Acceptance: A malformed import writes nothing. Reimporting prompts to copy, update, or cancel. Updating a plan never rewrites completed sessions.
DT001 Offline backup restore and export
Priority: MVP | Basis: Reference S8 and Proposal
Provide a versioned full backup containing exercises, plans, sessions, sets, settings, measurements, and meal plans when present. Use a user-selected file destination. Offer preview and explicit replace confirmation on restore; validate before replacement. CSV exports are Next and are not full restore files.
Acceptance: A backup restored into a clean install preserves IDs, relationships, units, and completion states. An invalid backup leaves current data unchanged.
DT002 Privacy and upgrades
Priority: MVP | Basis: Proposal
Keep primary data locally and make outbound sharing user initiated. Store no secrets in the repository. Database upgrades preserve existing results through tested migrations. Request permissions only for enabled features.
Acceptance: Offline logging works without an account. An upgrade retains a populated test database. A denied optional permission leaves core logging usable.

 Implementation and release acceptance
Proposed implementation direction
Kotlin, Jetpack Compose, ViewModels, and Room local storage. Retain the existing Android project configuration until the actual repository is inspected. The owner selected API 36 when creating the project; verify minimum, compile, and target values in Gradle rather than treating earlier conversation values as authoritative. Platform-specific timer and notification behavior requires Android implementation validation.
Core data records
Record	Required relationships or values
Category and Exercise	Stable IDs; origin; archived flag; type; unit; load convention.
Plan and PlanExercise	Order; target sets; rest; optional group; plan version.
Session and SessionExercise	Workout date; timestamps; status; prescription snapshot.
SetEntry	Session exercise ID; ordinal; actual values; unit; completion; notes.
TimerState	Owner session; active deadline or paused remaining time.
Measurement and MealPlan	Dated user records; units or food portions; editable targets.
Release gate scenarios
•	Log, restart, edit, and finish a Wednesday session offline without data loss.
•	Run rest timing while foregrounded, backgrounded, locked, rotated, and reopened after expiry.
•	Switch between barbell, dumbbell, machine, bodyweight, and time entries without ambiguous fields.
•	Archive an exercise used in history; edit a plan without altering an old session.
•	Round-trip a populated full backup; reject a damaged backup without changing local data.
•	Verify empty states, partial sessions, duplicate taps, enlarged text, and local-date boundaries.
•	Verify progression with 41 reps, five tens, unequal set reps totaling 50, mixed loads, warmups, and declined suggestions.
Proposed performance goals: warm app navigation under 1 second and confirmed local saves under 500 ms on the owner’s phone. Measure before release; these are targets rather than verified results.

 Owner decisions and source register
Decision	Proposed answer to edit
Meal-plan release	Next; move into MVP if required.
Categories	Core replaces Abs; add Stretches.
Multiple muscles	One primary category now; secondary tags later.
Library deletion	Archive used entries; hide built-ins.
Active sessions	One active session; multiple completed sessions per date.
Timer behavior	Optional auto-start; show permission limits; one active timer.
Imports	Versioned JSON first; document extraction later.
Body entry location	History Body tab and secondary home shortcut.
Personal setup	Optional private seed data; confirm dates, loads, and missing rest values.
FitNotes reference mapping
Reviewed official Android product and help pages on October 12 2026. FitNotes is a feature reference; proposed release scope, data model, edge cases, meal planning, and progression behavior are LiftHappens decisions. Documentation is not a complete inventory of the current installed app. Some FitNotes exercise types and per-exercise units are marked Supporter; LiftHappens pricing is undecided.
S0 Product overview — https://www.fitnotesapp.com/
S1 Exercises — https://www.fitnotesapp.com/exercises/
S2 Routines — https://www.fitnotesapp.com/routines/
S3 Workout tracking — https://www.fitnotesapp.com/workout_tracking/
S4 Workout tools — https://www.fitnotesapp.com/workout_tools/
S5 Calendar — https://www.fitnotesapp.com/calendar/
S6 Progress tracking — https://www.fitnotesapp.com/progress_tracking/
S7 Body tracker — https://www.fitnotesapp.com/body_tracker/
S8 Settings and backups — https://www.fitnotesapp.com/settings/
S9 Home screen — https://www.fitnotesapp.com/home_screen/
