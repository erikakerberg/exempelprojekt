package se.sunstone.msa;

import com.canto.cumulus.*;
import com.canto.cumulus.constants.FindFlag;
import com.canto.cumulus.exceptions.LoginFailedException;
import com.canto.cumulus.exceptions.PasswordExpiredException;
import com.canto.cumulus.exceptions.ServerNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.EnumSet;

public class CumConnection {

    static Server server;
    static ArrayList<CatalogHandler> catalogs = new ArrayList<>();
    static ArrayList<Integer> catalogIds = new ArrayList<>();

    static Logger logger = LoggerFactory.getLogger(CumConnection.class);

    public static void startConnection() throws ServerNotFoundException, PasswordExpiredException, LoginFailedException {

        Cumulus.CumulusStart();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                Cumulus.CumulusStop();
            }
        });

        server = Server.openConnection(
                true,
                Config.CUMULUS_HOST_IP,
                Config.CUMULUS_USERNAME,
                Config.CUMULUS_PASSWORD
        );

        for (String catalogName : Config.CUMULUS_CATALOG_NAMES) {
            int catalogId = server.findCatalogID(catalogName);
            catalogIds.add(catalogId);
            String catalogKey = Config.CUMULUS_CATALOG_KEYS.get(Config.CUMULUS_CATALOG_NAMES.indexOf(catalogName));
            catalogs.add(new CatalogHandler(server.openCatalog(catalogId), catalogKey));
        }

    }

    public static ArrayList<ItemHandler> getItemsFromIngestStatus(String status) {
        EnumSet<FindFlag> flags = EnumSet.noneOf(FindFlag.class);

        ArrayList<ItemHandler> foundItems = new ArrayList<>();

        for (CatalogHandler catalog : catalogs) {
            ItemCollection catalogItems = catalog.cumCatalog.newItemCollection(
                    Cumulus.TABLE_NAME_ASSET_RECORDS,
                    catalog.ingestStatusField + " == " + status,
                    flags,
                    null
            );

            for (Item item : catalogItems) {
                foundItems.add(new ItemHandler(item));
            }
        }

        return foundItems;
    }

    public static ArrayList<ItemHandler> getItemsFromArchivingStatus(String status) {
        EnumSet<FindFlag> flags = EnumSet.noneOf(FindFlag.class);

        ArrayList<ItemHandler> foundItems = new ArrayList<>();

        for (CatalogHandler catalog : catalogs) {
            ItemCollection catalogItems = catalog.cumCatalog.newItemCollection(
                    Cumulus.TABLE_NAME_ASSET_RECORDS,
                    catalog.archivingStatusField + " == " + status,
                    flags,
                    null
            );

            for (Item item : catalogItems) {
                foundItems.add(new ItemHandler(item));
            }
        }

        return foundItems;
    }
    public static ArrayList<ItemHandler> getItemsFromLrArchivingStatus(String status) {
        EnumSet<FindFlag> flags = EnumSet.noneOf(FindFlag.class);

        ArrayList<ItemHandler> foundItems = new ArrayList<>();

        for (CatalogHandler catalog : catalogs) {
            ItemCollection catalogItems = catalog.cumCatalog.newItemCollection(
                    Cumulus.TABLE_NAME_ASSET_RECORDS,
                    catalog.lrArchivingStatusField + " == " + status,
                    flags,
                    null
            );

            for (Item item : catalogItems) {
                foundItems.add(new ItemHandler(item));
            }
        }

        return foundItems;
    }

    public static ArrayList<ItemHandler> getItemsFromHrArchivingStatus(String status) {
        EnumSet<FindFlag> flags = EnumSet.noneOf(FindFlag.class);

        ArrayList<ItemHandler> foundItems = new ArrayList<>();

        for (CatalogHandler catalog : catalogs) {
            ItemCollection catalogItems = catalog.cumCatalog.newItemCollection(
                    Cumulus.TABLE_NAME_ASSET_RECORDS,
                    catalog.hrArchivingStatusField + " == " + status,
                    flags,
                    null
            );

            for (Item item : catalogItems) {
                foundItems.add(new ItemHandler(item));
            }
        }

        return foundItems;
    }


    public static ArrayList<ItemHandler> getItemsToDelete() {
        EnumSet<FindFlag> flags = EnumSet.noneOf(FindFlag.class);

        ArrayList<ItemHandler> foundItems = new ArrayList<>();

        for (CatalogHandler catalog : catalogs) {
            ItemCollection catalogItems = catalog.cumCatalog.newItemCollection(
                    Cumulus.TABLE_NAME_ASSET_RECORDS,
                    catalog.deleteField + " == True",
                    flags,
                    null
            );

            for (Item item : catalogItems) {
                foundItems.add(new ItemHandler(item));
            }
        }

        return foundItems;
    }

    public static ItemHandler searchForItemById(String id) {
        EnumSet<FindFlag> flags = EnumSet.noneOf(FindFlag.class);

        ArrayList<ItemHandler> foundItems = new ArrayList<>();

        for (CatalogHandler catalog : catalogs) {
            ItemCollection catalogItems = catalog.cumCatalog.newItemCollection(
                    Cumulus.TABLE_NAME_ASSET_RECORDS,
                    catalog.idField + " == " + id,
                    flags,
                    null
            );

            for (Item item : catalogItems) {
                foundItems.add(new ItemHandler(item));
            }
        }

        if (foundItems.size() > 1) {
            logger.error("Multiple items with same ID: " + id);
            return null;
        } else if (foundItems.size() == 0) {
            logger.error("No item with ID: " + id);
            return null;
        }

        return foundItems.get(0);
    }
}
