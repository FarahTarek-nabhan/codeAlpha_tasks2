import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;

class Room implements Serializable {
    int number;
    String type;
    double price;
    boolean available;

    Room(int number, String type, double price, boolean available) {
        this.number = number;
        this.type = type;
        this.price = price;
        this.available = available;
    }
}

class Reservation implements Serializable {
    String guestName;
    int roomNumber;
    String checkIn;
    String checkOut;

    Reservation(String guestName, int roomNumber, String checkIn, String checkOut) {
        this.guestName = guestName;
        this.roomNumber = roomNumber;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
    }
}

public class HotelReservationGUI extends JFrame {
    private ArrayList<Room> rooms = new ArrayList<>();
    private ArrayList<Reservation> reservations = new ArrayList<>();
    private JTable table;
    private DefaultTableModel model;

    private final String ROOMS_FILE = "rooms.dat";
    private final String RESERVATIONS_FILE = "reservations.dat";

    public HotelReservationGUI() {
        setTitle("Hotel Reservation System");
        setSize(700, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        loadData();

        JPanel panel = new JPanel(new BorderLayout());

        model = new DefaultTableModel(new String[]{"Room #", "Type", "Price", "Available"}, 0);
        table = new JTable(model);
        refreshTable();
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel();

        JButton btnBook = new JButton("Book Room");
        btnBook.addActionListener(e -> bookRoom());
        buttonsPanel.add(btnBook);

        JButton btnCancel = new JButton("Cancel Reservation");
        btnCancel.addActionListener(e -> cancelReservation());
        buttonsPanel.add(btnCancel);

        JButton btnExit = new JButton("Exit");
        btnExit.addActionListener(e -> saveAndExit());
        buttonsPanel.add(btnExit);

        panel.add(buttonsPanel, BorderLayout.SOUTH);
        add(panel);

        if (rooms.isEmpty()) {
            rooms.add(new Room(101, "Single", 50.0, true));
            rooms.add(new Room(102, "Double", 75.0, true));
            rooms.add(new Room(103, "Suite", 150.0, true));
            rooms.add(new Room(104, "Single", 50.0, true));
            saveData();
            refreshTable();
        }
    }

    private void refreshTable() {
        model.setRowCount(0);
        for (Room room : rooms) {
            model.addRow(new Object[]{room.number, room.type, room.price, room.available ? "Yes" : "No"});
        }
    }

    private void bookRoom() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a room to book.");
            return;
        }

        int roomNumber = (int) model.getValueAt(selectedRow, 0);
        Room selectedRoom = null;
        for (Room r : rooms) {
            if (r.number == roomNumber) {
                selectedRoom = r;
                break;
            }
        }

        if (selectedRoom != null && selectedRoom.available) {
            String guestName = JOptionPane.showInputDialog(this, "Enter Guest Name:");
            String checkIn = JOptionPane.showInputDialog(this, "Enter Check-in Date (dd/mm/yyyy):");
            String checkOut = JOptionPane.showInputDialog(this, "Enter Check-out Date (dd/mm/yyyy):");

            if (guestName != null && checkIn != null && checkOut != null) {
                reservations.add(new Reservation(guestName, roomNumber, checkIn, checkOut));
                selectedRoom.available = false;
                saveData();
                refreshTable();
                JOptionPane.showMessageDialog(this, "Room booked successfully!");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Room is not available.");
        }
    }

    private void cancelReservation() {
        String guestName = JOptionPane.showInputDialog(this, "Enter Guest Name to Cancel:");
        if (guestName == null) return;

        Reservation found = null;
        for (Reservation res : reservations) {
            if (res.guestName.equalsIgnoreCase(guestName)) {
                found = res;
                break;
            }
        }

        if (found != null) {
            for (Room r : rooms) {
                if (r.number == found.roomNumber) {
                    r.available = true;
                    break;
                }
            }
            reservations.remove(found);
            saveData();
            refreshTable();
            JOptionPane.showMessageDialog(this, "Reservation canceled.");
        } else {
            JOptionPane.showMessageDialog(this, "No reservation found for that name.");
        }
    }

    private void saveData() {
        try (ObjectOutputStream oos1 = new ObjectOutputStream(new FileOutputStream(ROOMS_FILE));
             ObjectOutputStream oos2 = new ObjectOutputStream(new FileOutputStream(RESERVATIONS_FILE))) {
            oos1.writeObject(rooms);
            oos2.writeObject(reservations);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadData() {
        try (ObjectInputStream ois1 = new ObjectInputStream(new FileInputStream(ROOMS_FILE));
             ObjectInputStream ois2 = new ObjectInputStream(new FileInputStream(RESERVATIONS_FILE))) {
            rooms = (ArrayList<Room>) ois1.readObject();
            reservations = (ArrayList<Reservation>) ois2.readObject();
        } catch (Exception e) {
            rooms = new ArrayList<>();
            reservations = new ArrayList<>();
        }
    }

    private void saveAndExit() {
        saveData();
        System.exit(0);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new HotelReservationGUI().setVisible(true));
    }
}
