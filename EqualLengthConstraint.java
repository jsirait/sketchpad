public class EqualLengthConstraint implements Constraint { 
    private LineObject line1; 
    private LineObject line2; 

    public EqualLengthConstraint(LineObject line1, LineObject line2) {
        this.line1 = line1;
        this.line2 = line2;
    } 

    @Override 
    public void apply() {
        // placeholder 
        System.out.println("Equal length constrint to be applied"); 
    }
    
}
