package se.sunstone.msa;

import java.util.ArrayList;

public class DeleteChecker {
    public static ArrayList<ItemHandler> run() {
        ArrayList<ItemHandler> itemsToDelete = CumConnection.getItemsToDelete();
        ArrayList<ItemHandler> deletedItems = new ArrayList<>();

        for (ItemHandler itemToDelete : itemsToDelete) {
            if (itemToDelete.deleteWorkingFiles()) {
                itemToDelete.deleteEncryptedFiles();
                itemToDelete.deleteOriginalAsset();
                itemToDelete.save();
                deletedItems.add(itemToDelete);
            }
        }

        return deletedItems;
    }
}
