# Git Commit Workflow

Always follow these Git commit rules when modifying the codebase:
1. **Before making any changes**: Check if there are unstaged changes. If so, ask the user in the chat what commit message they would like to use for saving the current state, make a git commit with that message, and then proceed with the edits.
2. **After finishing changes**: Create a git commit containing all modified/new files, with the commit message formatted as: `Gemini: <detailed summary of what you actually did>`.
