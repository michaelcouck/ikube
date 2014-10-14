package ikube.example;

/**
 * This example is for a recommendation to the client, there are three analyses, predicting
 * other items that are similar to the one selected, predicting other items that are related to the
 * item selected and predicting items that other users that bought the item selected also bought.
 * <p/>
 * The process is then to vote for the top items in the predictions, and suggest the
 * top results to the user.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 14-10-2014
 */
public class Recommendation extends Base {

    private static final String[] CATEGORIES = {"clothing", "food", "holidays"};
    private static final String[] SUB_CATEGORIES = {"shoes", "pants", "shirts", "italian", "chinese", "french", "family", "adventure", "ski"};
    private static final String[] SUB_SUB_CATEGORIES = {
            "running", "cross-training", "formal",
            "track", "formal", "corduroy",
            "t-shirt", "dress", "casual",
            "pasta", "anti-pasta", "fish",
            "pork", "duck", "beef",
            "stew", "baked", "grilled",
            "5-star", "4-star", "3-star"};
    private static final String[] NAMES = {"Nike", "Ascics", "New Balance"};

    public static void main(final String[] args) {
        // 1) Populate a classifier with product data for the similar items prediction,
        // i.e. if Nike running shoes were bought, then suggest New Balance running shoes

        // 2) Populate a clusterer with product data that is related to the items bought,
        // i.e. clusters where shoes and socks reside

        // Populate a clusterer of users based on the goods that they bought, and clicked on
        // then find the cluster where the user is and get the items that the nearest neighbour
        // bought
    }

    private void predictSimilarItems() {
        // id,category,sub-category,sub-sub-category,colour,name,price
        // 1,clothing,shoe,running,red,Nike,67.56

    }

    private void predictRelatedItems() {
        // id,category,sub-category,sub-sub-category,colour,name,price
        // 1,clothing,shoes,running,black,Nike,78.12
    }

    private void predictSimilarUsersAndTheirItems() {
        // id,name,bought,looked-at,age,location
        // 1,Michael Couck,2|5,4,43,Belgium
    }

    private Object[][] generateProductData() {
        Object[][] matrix = new Object[CATEGORIES.length * SUB_CATEGORIES.length * SUB_SUB_CATEGORIES.length][];
        for (int i = 0; i < CATEGORIES.length; i++) {
            for (int j = 0; j < SUB_CATEGORIES.length; j++) {
                for (int k = 0; k < SUB_SUB_CATEGORIES.length; k++) {
                    Object[] product = new Object[7];
                    matrix[i] = product;
                }
            }
        }
        return matrix;
    }

}