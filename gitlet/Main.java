package gitlet;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Wilson Chu
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        switchUpToGlobal(args);
    }

    /**
     * Throws the error "Incorrect operands.".
     */
    public static void incorrectOperandsError() {
        System.out.println("Incorrect operands.");
        System.exit(0);
    }

    /**
     * Passing off some of the switch/case statements to
     * this helper function.
     * @param args The user input.
     */
    private static void switchUpToGlobal(String... args) {
        switch (args[0]) {
        case "init":
            if (args.length != 1) {
                incorrectOperandsError();
            }
            Repo.initialize();
            break;
        case "add":
            if (args.length != 2) {
                incorrectOperandsError();
            }
            Repo.initializedCheck();
            Repo.add(args[1]);
            break;
        case "commit":
            if (args.length < 2) {
                System.out.println("Please enter a commit message.");
                System.exit(0);
            } else if (args.length > 2) {
                incorrectOperandsError();
            }
            Repo.initializedCheck();
            Repo.commit(args[1]);
            break;
        case "rm":
            Repo.initializedCheck();
            if (args.length != 2) {
                incorrectOperandsError();
            }
            Repo.rm(args[1]);
            break;
        case "log":
            Repo.initializedCheck();
            Repo.log();
            break;
        case "global-log":
            if (args.length != 1) {
                incorrectOperandsError();
            }
            Repo.initializedCheck();
            Repo.globalLog();
            break;
        default: {
            switchFromFindOnwards(args);
        }
        }
    }

    /**
     * Passing off the remaining switch/case statements to this
     * helper function.
     * @param args The user input.
     */
    public static void switchFromFindOnwards(String... args) {
        switch (args[0]) {
        case "find":
            Repo.initializedCheck();
            if (args.length != 2) {
                incorrectOperandsError();
            }
            Repo.find(args[1]);
            break;
        case "status":
            Repo.initializedCheck();
            if (args.length != 1) {
                incorrectOperandsError();
            }
            System.out.println(Repo.getStagingArea());
            break;
        case "checkout":
            Repo.initializedCheck();
            checkout(args);
            break;
        case "branch":
            Repo.initializedCheck();
            Repo.branch(args[1]);
            break;
        case "rm-branch":
            Repo.initializedCheck();
            if (args.length != 2) {
                incorrectOperandsError();
            }
            Repo.rmBranch(args[1]);
            break;
        case "reset":
            Repo.initializedCheck();
            if (args.length != 2) {
                incorrectOperandsError();
            }
            Repo.reset(args[1]);
            break;
        case "merge":
            Repo.initializedCheck();
            if (args.length != 2) {
                incorrectOperandsError();
            }
            Repo.merge(Repo.getActiveBranch().getName(), args[1]);
            break;
        default: {
            System.out.println("No command with that name exists.");
            System.exit(0);
        }
        }
    }

    /**
     * Passing off the checkout case from our switch cases.
     * @param args The user input.
     */
    private static void checkout(String... args) {
        if (args.length == 3) {
            if (!args[1].equals("--") || args[2].equals("--")) {
                incorrectOperandsError();
            } else {
                Repo.checkoutRevert(args[2]);
            }
        } else if (args.length == 4) {
            if (!args[2].equals("--") || args[1].equals("--")
                    || args[3].equals("--")) {
                incorrectOperandsError();
            } else {
                Repo.checkoutFromCommit(args[1], args[3]);
            }
        } else if (args.length == 2) {
            Repo.checkoutBranch(args[1]);
        }
    }

}
