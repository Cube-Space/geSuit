package net.cubespace.geSuit.listeners;

import net.cubespace.geSuit.FeatureDetector;
import net.cubespace.geSuit.Utilities;
import net.cubespace.geSuit.managers.DatabaseManager;
import net.cubespace.geSuit.managers.HomesManager;
import net.cubespace.geSuit.managers.LoggingManager;
import net.cubespace.geSuit.managers.PlayerManager;
import net.cubespace.geSuit.objects.GSPlayer;
import net.cubespace.geSuit.objects.Location;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.sql.SQLException;

public class HomesMessageListener implements Listener {

    @EventHandler
    public void receivePluginMessage( PluginMessageEvent event ) throws IOException, SQLException {
        if ( event.isCancelled() ) {
            return;
        }

        if ( !( event.getSender() instanceof Server ) )
            return;

        if ( !event.getTag().equalsIgnoreCase( "geSuitHomes" ) ) {
            return;
        }

        event.setCancelled( true );

        DataInputStream in = new DataInputStream( new ByteArrayInputStream( event.getData() ) );

        String task = in.readUTF();

        if ( task.equals( "DeleteHome" ) ) {
            HomesManager.deleteHome(in.readUTF(), in.readUTF() );
        } else if ( task.equals( "SendPlayerHome" ) ) {
            HomesManager.sendPlayerToHome( PlayerManager.getPlayer(in.readUTF() ), in.readUTF() );
        } else if ( task.equals( "SetPlayersHome" ) ) {
            String player = in.readUTF();
            GSPlayer gsPlayer = PlayerManager.getPlayer(player);

            if (gsPlayer == null) {
                gsPlayer = DatabaseManager.players.loadPlayer(player);

                if (gsPlayer == null) {
                    DatabaseManager.players.insertPlayer(new GSPlayer(player, (FeatureDetector.canUseUUID()) ? Utilities.getUUID(player) : null, true), "0.0.0.0");
                    gsPlayer = DatabaseManager.players.loadPlayer(player);
                    gsPlayer.setServer(((Server) event.getSender()).getInfo().getName());
                } else {
                    gsPlayer.setServer(((Server) event.getSender()).getInfo().getName());
                }
            }

            HomesManager.createNewHome(gsPlayer, in.readInt(), in.readInt(), in.readUTF(), new Location(((Server) event.getSender()).getInfo().getName(), in.readUTF(), in.readDouble(), in.readDouble(), in.readDouble(), in.readFloat(), in.readFloat()));
        } else if ( task.equals( "GetHomesList" ) ) {
            HomesManager.listPlayersHomes( PlayerManager.getPlayer( in.readUTF() ) );
        } else if ( task.equals( "SendVersion" ) ) {
            LoggingManager.log( in.readUTF() );
        }

        in.close();
    }
}