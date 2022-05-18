public class MainClass {

    public static void main(String[] args) {
        Action action = Action.valueOf(args[0]);
        String inputPath = args[1];
        String outputPath;
        if (args.length==3) {
            outputPath = args[2];
        }

        if (action.equals(Action.load)) {

        } else if (action.equals(Action.create)) {

            
        }


    }

    private enum Action {
        load,
        create
    }
}
