# FP Hangman

Hangman game applying some FP concepts in Java

## Initial requirements of Hangman game for CLI:

    - Ask Player's name
    - Pick a word at random from a Dictionary
    - Initialize Game
    - Display initial state
    - (Game Loop)
        - Ask for a letter (or non-letter for abandoning)
        - Evaluate letter
            - Selection is non-letter -> ABANDON (update state)
            - Selection is already selected -> CONTINUE (no update of state)
            - Selection is not selected and present in the word:
                - No more letters to guess -> WON (update state)
                - More letters to guess -> CONTINUE (update state)
            - Selection is not selected and not present in the word:
                - No more tries available (met threshold) -> LOST (update state)
                - More tries available -> CONTINUE (update state)
    - Display the outcome

## Language to be used
Initially, Java 8. After concepts are clearer, start transitioning into higher versions of Java, Scala or Kotlin.

## Approach
We'll be introducing the concepts one by one.

### Sessions

[First session](./docs/first-session.md)
[Second session](./docs/second-session.md)