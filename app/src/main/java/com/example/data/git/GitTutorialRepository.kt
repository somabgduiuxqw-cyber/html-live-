package com.example.data.git

object GitTutorialRepository {
    val topics = listOf(
        GitTutorialTopic(
            id = "what_is_git",
            title = "What is Git?",
            subtitle = "Distributed Version Control System",
            explanation = "Git is a distributed version control system that tracks changes in any set of computer files. Unlike centralized systems, every developer has a complete copy of the code history on their local machine. In HTML Live, Git allows you to test experiments, revert mistakes, and collaborate seamlessly.",
            commandExample = "git --version\ngit init",
            practicalTip = "Think of Git as a powerful time machine for your website code. You can travel back to any save point without losing your work."
        ),
        GitTutorialTopic(
            id = "what_is_github",
            title = "What is GitHub?",
            subtitle = "Cloud Platform for Git Repositories",
            explanation = "GitHub is a cloud platform that hosts Git repositories online. It provides web interfaces, collaboration tools, issue tracking, and allows anyone to share open-source web projects or backup private source code.",
            commandExample = "https://github.com/username/my-web-project.git",
            practicalTip = "GitHub stores the remote copy of your repository in the cloud, while HTML Live manages your active local copy."
        ),
        GitTutorialTopic(
            id = "what_is_repo",
            title = "What is a Repository?",
            subtitle = "Project Folder with .git Tracking",
            explanation = "A repository (or repo) is the complete collection of project files plus the hidden .git directory containing the commit history, branches, and configuration. In HTML Live, each cloned project has its own isolated repository directory.",
            commandExample = "📁 my-website/\n  ├── 📁 .git/ (Git history)\n  ├── index.html\n  ├── style.css\n  └── script.js",
            practicalTip = "Never delete the .git folder unless you intentionally want to turn a Git repository into plain non-tracked files."
        ),
        GitTutorialTopic(
            id = "what_is_cloning",
            title = "What is Cloning?",
            subtitle = "Downloading Repo with Full History",
            explanation = "Cloning downloads an entire remote repository from the internet to your local device. It copies all code files, assets, historical commits, and default branches into HTML Live so you can preview and edit it offline.",
            commandExample = "git clone https://github.com/username/project.git",
            practicalTip = "HTML Live automatically inspects cloned projects for index.html, package.json, and Canvas games to let you run them immediately."
        ),
        GitTutorialTopic(
            id = "what_is_branch",
            title = "What is a Branch?",
            subtitle = "Parallel Timelines for Features",
            explanation = "A branch represents an independent line of development. The default branch is usually named 'main'. Creating a feature branch (e.g. 'dev' or 'feature/mobile-ui') allows you to build new features without affecting your stable website.",
            commandExample = "git branch feature/mobile-ui\ngit checkout feature/mobile-ui",
            practicalTip = "Always create a new branch when trying risky design redesigns or big JavaScript refactors."
        ),
        GitTutorialTopic(
            id = "what_is_commit",
            title = "What is a Commit?",
            subtitle = "Permanent Snapshot of Changes",
            explanation = "A commit is a permanent snapshot of staged file changes with a descriptive message and timestamp. Each commit is identified by a unique cryptographic SHA hash ID.",
            commandExample = "git add index.html style.css\ngit commit -m \"Added mobile responsive navbar\"",
            practicalTip = "Write clear, imperative commit messages like 'Fix button click sound' rather than 'changes'."
        ),
        GitTutorialTopic(
            id = "what_is_push",
            title = "What is Push?",
            subtitle = "Uploading Commits to Remote",
            explanation = "Pushing uploads your local commits to the remote repository (such as GitHub). HTML Live requires explicit confirmation before pushing to ensure you never overwrite changes accidentally.",
            commandExample = "git push origin main",
            practicalTip = "Make sure you have configured a Personal Access Token (PAT) for private repositories or authenticated GitHub accounts."
        ),
        GitTutorialTopic(
            id = "what_is_pull",
            title = "What is Pull?",
            subtitle = "Fetching and Merging Remote Updates",
            explanation = "Pulling downloads changes made by collaborators or remote updates and merges them directly into your local working tree. HTML Live displays which files were modified (M), added (A), or deleted (D).",
            commandExample = "git pull origin main",
            practicalTip = "Always pull before you start working on a project if you or your teammates push code from other computers."
        ),
        GitTutorialTopic(
            id = "what_is_merge",
            title = "What is Merge?",
            subtitle = "Combining Two Branches",
            explanation = "Merging combines the commit history and file changes from one branch into another (for example, merging 'feature/dark-mode' into 'main'). If there are no conflicting edits on the same lines, Git combines them automatically.",
            commandExample = "git checkout main\ngit merge feature/dark-mode",
            practicalTip = "Review Git Diff before completing merges to verify which lines will change."
        ),
        GitTutorialTopic(
            id = "what_is_conflict",
            title = "What is a Conflict?",
            subtitle = "When Two Edits Clash on Same Lines",
            explanation = "A merge conflict occurs when the same line of a file was changed differently in local and remote commits. Git pauses and places conflict markers (<<<<<<< HEAD, =======, >>>>>>>). HTML Live provides a built-in Conflict Editor to resolve it easily.",
            commandExample = "<<<<<<< HEAD\n<h1>My Local Title</h1>\n=======\n<h1>Remote Updated Title</h1>\n>>>>>>> origin/main",
            practicalTip = "Use the Conflict Editor to choose 'Keep Local', 'Keep Remote', or edit the markup manually, then save."
        ),
        GitTutorialTopic(
            id = "what_is_gitignore",
            title = "What is .gitignore?",
            subtitle = "Preventing Unwanted Files from Tracking",
            explanation = "A .gitignore file specifies intentionally untracked files that Git should ignore (such as node_modules, build outputs, temporary logs, or OS files). HTML Live respects .gitignore and keeps ignored files safe without tracking them.",
            commandExample = "# .gitignore\nnode_modules/\n.DS_Store\n*.log\ndist/",
            practicalTip = "Never commit secret keys or sensitive configuration files—always put them in .gitignore!"
        )
    )
}
