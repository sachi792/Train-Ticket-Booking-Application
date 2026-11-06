import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

// Quest 03 railway booking system 

public class RailwaySystem extends JFrame {

    private final List<Train> trains = new ArrayList<>();

    private final List<Booking> bookings = new ArrayList<>();

    private final JComboBox<String> routeBox = new JComboBox<>(),trainBox=new JComboBox<>();

    private final JPanel seatPanel = new JPanel();

    private final JLabel info = new JLabel("Select a train"), selection = new JLabel("Selected: none");

    private final JTextField idField = new JTextField(15),nameField = new JTextField(15),phoneField = new JTextField(15),bookingField = new JTextField(12);

    private final DefaultTableModel model = new DefaultTableModel(new String[]{"Booking ID","Train","Customer ID","Name","Phone","Seats"},0){public boolean isCellEditable(int r,int c){return false;}};
     
    private final JTable table = new JTable(model);

    private final LinkedHashSet<Integer> chosen = new LinkedHashSet<>();

    private boolean updating = false;

    private static final Color BLUE = new Color(29,78,137), FREE = new Color(194,235,210), TAKEN = new Color(242,170,170);



    public RailwaySystem(){
        super("Sri Lanka Railways | Ticket Booking Officer");
        loadTrains(); 
        loadBookings();
        reconcileCounts();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100,760);
        setMinimumSize(new Dimension(800,580));
        setLocationRelativeTo(null);
        
        JPanel root = new JPanel(new BorderLayout(10,10));
        root.setBorder(BorderFactory.createEmptyBorder(14,14,14,14));
        setContentPane(root);
        
        JLabel title=new JLabel("SRI LANKA RAILWAYS  •  EXPRESS TRAIN RESERVATIONS");
        title.setFont(new Font("SansSerif",Font.BOLD,20));
        title.setForeground(BLUE);
        
        JPanel top = new JPanel(new BorderLayout(0,12));
        top.add(title,BorderLayout.NORTH);
        
        JPanel filters=new JPanel(new FlowLayout(FlowLayout.LEFT,12,0));
        filters.add(new JLabel("Route:"));
        filters.add(routeBox);
        filters.add(new JLabel("Train:"));
        filters.add(trainBox);
        filters.add(info);
        top.add(filters,BorderLayout.SOUTH);root.add(top,BorderLayout.NORTH);

        for(Train t:trains)if(((DefaultComboBoxModel<String>)routeBox.getModel()).getIndexOf(t.getRoute())<0)routeBox.addItem(t.getRoute());

        routeBox.addActionListener(e->populateTrains());trainBox.addActionListener(e->refresh());
        seatPanel.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        
        JScrollPane seatScroll=new JScrollPane(seatPanel);
        seatScroll.setBorder(BorderFactory.createTitledBorder("Seat map  •  green: available  |  red: booked  |  blue: selected"));
        
        JPanel actions=new JPanel(new BorderLayout(8,8));
        
        JPanel fields=new JPanel(new GridLayout(2,4,8,5));
        fields.add(new JLabel("Customer NIC / ID"));
        fields.add(new JLabel("Customer name"));
        fields.add(new JLabel("Contact number"));
        fields.add(new JLabel("Booking reference (edit/cancel)"));
        fields.add(idField);
        fields.add(nameField);
        fields.add(phoneField);
        fields.add(bookingField);
        actions.add(fields,BorderLayout.NORTH);
        
        JPanel buttons=new JPanel(new FlowLayout(FlowLayout.LEFT,8,5));
        
        JButton book = button("Book selected seats"),find = button("Find booking"),modify = button("Change booking seats"),cancel = button("Cancel booking"),clear=button("Clear selection");
        buttons.add(book);buttons.add(find);buttons.add(modify);
        buttons.add(cancel);buttons.add(clear);
        buttons.add(selection);
        actions.add(buttons,BorderLayout.CENTER);
        book.addActionListener(e->makeBooking());
        find.addActionListener(e->findBooking());
        modify.addActionListener(e->modifyBooking());
        cancel.addActionListener(e->cancelBooking());
        clear.addActionListener(e->{chosen.clear();
            refresh();});
        
        JPanel lower = new JPanel(new BorderLayout(6,6));
        lower.add(actions,BorderLayout.NORTH);
        table.setAutoCreateRowSorter(true);
        table.setRowHeight(24);
        table.getSelectionModel().addListSelectionListener(e->{if(!e.getValueIsAdjusting()&&table.getSelectedRow()>=0){int r=table.convertRowIndexToModel(table.getSelectedRow());bookingField.setText((String)model.getValueAt(r,0));}});
        lower.add(new JScrollPane(table),BorderLayout.CENTER);
        lower.setBorder(BorderFactory.createTitledBorder("Customer details and bookings for selected train"));
        
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,seatScroll,lower);
        split.setResizeWeight(.56);
        split.setDividerLocation(370);
        root.add(split,BorderLayout.CENTER);
        populateTrains();

    }


    private JButton button(String s)
        {JButton b = new JButton(s);b.setFocusPainted(false);
            return b;    
        }

    private void populateTrains() {
        updating = true;
        trainBox.removeAllItems();
        String route=(String)routeBox.getSelectedItem();
        for(Train t:trains)if(t.getRoute().equals(route))trainBox.addItem(t.getTrainId()+" — "+t.getTrainName());
        updating=false;refresh();}
    
    private Train selectedTrain(){
        String s=(String)trainBox.getSelectedItem();
        if(s==null)return null;
        for(Train t:trains)
            if(s.startsWith(t.getTrainId()+" —"))
                return t;
        return null;
    }
    
    private int carriageCount(Train t) {
        switch(t.getTrainId()) {
            case "T101":return 5;case "T102":return 4;case "T201":return 6;case "T202":return 5;case "T301":return 5;case "T302":return 5;
            default:return 1;}
    }
    
    private Set<Integer> occupied(Train t,Booking exclude){
        Set<Integer> used=new HashSet<>();
        for(Booking b:bookings)
            if(b!=exclude&&b.getTrainId().equals(t.getTrainId()))
                used.addAll(b.getSeats());
            return used;
        }
    
    private void refresh(){
        if(updating)return;Train t=selectedTrain();
        seatPanel.removeAll();
        model.setRowCount(0);

        if(t==null){
            seatPanel.revalidate();
            seatPanel.repaint();
            return;
        }

        Set<Integer> used=occupied(t,null);
        chosen.removeIf(n->n<1||n>t.getTotalSeats()||used.contains(n));
        info.setText("Departure: "+t.getDepartureTime()+"  |  Available: "+(t.getTotalSeats()-used.size())+" / "+t.getTotalSeats());

        int carriages=carriageCount(t),per=t.getTotalSeats()/carriages;

        seatPanel.setLayout(new GridLayout(0,Math.min(carriages,3),10,10));
        for(int c=0;c<carriages;c++){
            JPanel carriage=new JPanel(new GridLayout(0,Math.min(per,5),4,4));
            carriage.setBorder(BorderFactory.createTitledBorder("Carriage "+(c+1)));
            for(int s=1;s<=per;s++){
                final int seat=c*per+s;JToggleButton b=new JToggleButton(""+s);
                b.setToolTipText("Carriage "+(c+1)+", seat "+s+" (global #"+seat+")");
                b.setOpaque(true);
                b.setSelected(chosen.contains(seat));
                b.setEnabled(!used.contains(seat));
                b.setBackground(used.contains(seat)?TAKEN:chosen.contains(seat)?new Color(141,188,247):FREE);
                b.addActionListener(e->{if(b.isSelected()){if(chosen.size()>=4){b.setSelected(false);error("Maximum 4 seats per booking.");
                return;}chosen.add(seat);}
                else chosen.remove(seat);
                refresh();});
                carriage.add(b);
            }seatPanel.add(carriage);
        }

        for(Booking b:bookings)
            if(b.getTrainId().equals(t.getTrainId()))
                model.addRow(new Object[]{b.getId(),b.getTrainId(),b.getCustomerId(),b.getName(),b.getPhone(),seatLabels(t,b.getSeats())});
        selection.setText("Selected: "+seatLabels(t,new ArrayList<>(chosen)));
        seatPanel.revalidate();
        seatPanel.repaint();
    }


    private String seatLabels(Train t,List<Integer> nums){
        int per=t.getTotalSeats()/carriageCount(t);
        StringJoiner j=new StringJoiner(", ");
        for(int n:nums)
            j.add("C"+((n-1)/per+1)+"-S"+((n-1)%per+1));
        return nums.isEmpty()?"none":j.toString();
    }
    
    private void makeBooking(){
        Train t=selectedTrain();

        if(t==null) 
            return;
        if(chosen.isEmpty())
            {error("Select 1–4 available seats on the seat map.");
            return;}

        String id = idField.getText().trim(),name = nameField.getText().trim(),phone = phoneField.getText().trim();
        
        if(!validCustomer(id,name,phone))
            return;

        int max=1000;
        
        for(Booking b:bookings)
            try{
            max=Math.max(max,Integer.parseInt(b.getId().replaceFirst("^B","")));
            }
        catch(NumberFormatException ignored){
        }
        
        String ref="B"+(max+1);
        Booking b=new Booking(ref,t.getTrainId(),id,name,phone,new ArrayList<>(chosen));
        bookings.add(b);

        if(!saveBookings()){bookings.remove(b);
            return;
        }
        chosen.clear();
        bookingField.setText(ref);
        refresh();
        message("Booking successful! Reference: "+ ref);
    }
    
    private boolean validCustomer(String id,String name,String phone){
        if(id.isEmpty()||name.isEmpty()||phone.isEmpty()){
        error("Enter customer ID, name, and contact number.");
        return false;
        }

        if(id.contains(",")||name.contains(",")||phone.contains(",")||id.contains(";")||name.contains(";")||phone.contains(";")){
        error("Customer fields cannot contain commas or semicolons.");
        return false;
        }

        return true;
    }
    
    private Booking bookingById(){
        String ref=bookingField.getText().trim();
        for(Booking b:bookings)
            if(b.getId().equalsIgnoreCase(ref))
                return b;
            error("Booking reference not found: "+ref);
            return null;
    }
    
    private void findBooking(){
        Booking b=bookingById();
        if(b==null)
            return;
        for(int i=0;i<routeBox.getItemCount(); i++)
            for(Train t:trains)
                if(t.getTrainId().equals(b.getTrainId())&&routeBox.getItemAt(i).equals(t.getRoute())){
                    routeBox.setSelectedIndex(i);
                    for(int k=0;k<trainBox.getItemCount();k++)
                        if(trainBox.getItemAt(k).startsWith(t.getTrainId()+" —"))trainBox.setSelectedIndex(k);
                    idField.setText(b.getCustomerId());
                    nameField.setText(b.getName());
                    phoneField.setText(b.getPhone());
                    chosen.clear();refresh();
                    message("Booking "+ b.getId()+" has seats: "+seatLabels(t,b.getSeats())+". Select replacement seats to modify.");
                    return;
                }
    }
    
    private void modifyBooking() {
        Booking b=bookingById();
        if(b==null)
            return;
        Train t=selectedTrain();
        if(t==null||!t.getTrainId().equals(b.getTrainId())){
            error("Select the booking's train first (use Find booking).");
            return;
        }
        if(chosen.isEmpty()){error("Select 1–4 replacement seats. Existing seats are shown in red.");
            return;
        }
        Set<Integer> others=occupied(t,b);
        if(others.stream().anyMatch(chosen::contains)){error("One or more selected seats belong to another booking.");
            return;
        }
        List<Integer> previous=new ArrayList<>(b.getSeats());
        b.getSeats().clear();
        b.getSeats().addAll(chosen);

        if(!saveBookings()){b.getSeats().clear();b.getSeats().addAll(previous);
            return;}chosen.clear();refresh();message("Booking "+b.getId()+" updated.");
        
    }
    
    private void cancelBooking() {
        Booking b=bookingById();
        if(b==null)return;
        if(JOptionPane.showConfirmDialog(this,"Cancel booking "+b.getId()+"?","Confirm cancellation",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)
            return;
        int idx=bookings.indexOf(b);
        bookings.remove(b);
        if(!saveBookings()){bookings.add(idx,b);
            return;
        }
        chosen.clear();
        refresh();
        message("Booking cancelled. Seats are available again.");
    }
    
    
    private void loadTrains(){
        File file=new File("timetables.txt");
        if(!file.exists()){error("Missing timetables.txt in working directory.");
            return;
        }

        try(BufferedReader r=new BufferedReader(new FileReader(file))){
            String line;
            while((line=r.readLine())!=null){String[] p=line.split(",",-1);
            if(p.length>=6)
                trains.add(new Train(p[0].trim(),p[1].trim(),p[2].trim(),p[3].trim(),Integer.parseInt(p[4].trim()),0));
            }
        }
        catch(Exception e){error("Could not read timetables: "+e.getMessage());}
    }
    
    private void loadBookings(){
        File file=new File("bookings.txt");
        if(!file.exists())
            return;
        try(BufferedReader r=new BufferedReader(new FileReader(file))){
            String line;
            while((line=r.readLine())!=null){
                String[] p=line.split(",",-1);
                if(p.length<6)continue;
                Train t=null;
                for(Train candidate:trains)
                    if(candidate.getTrainId().equals(p[1].trim()))
                        t=candidate;
                    if(t==null)
                        continue;
                
                List<Integer> seats=new ArrayList<>();
                Set<Integer> used=occupied(t,null);
                int count=Integer.parseInt(p[5].trim());

                if(p.length>=7&&!p[6].trim().isEmpty()){
                    for(String n:p[6].split(";")){
                        int v=Integer.parseInt(n.trim());if(v>=1&&v<=t.getTotalSeats()&&!used.contains(v)&&!seats.contains(v))seats.add(v);}}
                else for(int n=1;n<=t.getTotalSeats()&&seats.size()<count;n++)if(!used.contains(n))seats.add(n);
                if(!seats.isEmpty())
                    bookings.add(new Booking(p[0].trim(),p[1].trim(),p[2].trim(),p[3].trim(),p[4].trim(),seats));
            }
        }

        catch(Exception e){
            error("Could not load bookings: "+e.getMessage());}
    }
    
    private void reconcileCounts(){
        for(Train t:trains){
            int count=occupied(t,null).size();if(count>0)t.bookSeats(count);
        }
    }
    
    private boolean saveBookings(){
        File file=new File("bookings.txt"),temp=new File("bookings.txt.tmp");
        try(PrintWriter w=new PrintWriter(new FileWriter(temp))){
            for(Booking b:bookings)w.println(b.toFileString());
        }

        catch(IOException e){
                error("Could not save bookings: "+e.getMessage());return false;
        }

        try{
            java.nio.file.Files.move(temp.toPath(),file.toPath(),java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return true;
        }

        catch(IOException e){
                error("Could not replace bookings file: "+e.getMessage());return false;
            }
        
    }
    

    private void error(String s){
        JOptionPane.showMessageDialog(this,s,"Railway booking system",JOptionPane.ERROR_MESSAGE);
    }
    

    private void message(String s){
        JOptionPane.showMessageDialog(this,s,"Railway booking system",JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args){SwingUtilities.invokeLater(()->new RailwaySystem().setVisible(true));}

}
