public class EqualLengthConstraint implements Constraint { 
    private LineObject line1; 
    private LineObject line2; 

    public EqualLengthConstraint(LineObject line1, LineObject line2) {
        this.line1 = line1;
        this.line2 = line2;
    } 

    @Override 
    public boolean isSatisfied() {
        // int dx1 = line1.getx2() - line1.getx1();
        // int dy1 = line1.gety2() - line1.gety1(); 
        // int dx2 = line2.getx2() - line2.getx1(); 
        // int dy2 = line2.gety2() - line2.gety1(); 
        // int len1sq = dx1*dx1 + dy1*dy1; 
        // int len2sq = dx2*dx2 + dy2*dy2; 
        double len1sq = line1.getLength()*line1.getLength(); 
        double len2sq = line2.getLength()*line2.getLength();  

        return Math.abs(len1sq - len2sq) < 1; 
    }

    @Override 
    public void apply() {
        // placeholder 
        System.out.println("Equal length constrint to be applied"); 
    }
    
}
