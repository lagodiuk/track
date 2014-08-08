$(document).ready(function() {
    
	$('button').click(function(){
    	
		var max = 0;
		for(var i in visible) {
			if(visible[i] > max) {
				max = visible[i];
			}
		}

		$('.track').each(function() {
    		
    		var counter = $(this).attr('counter');
    		if(!visible[counter]) {
    			return;
    		}
    		
    		$(this).css('background-color', 'rgba(250, 0, 0, ' + ( visible[counter] / max ) + ')');		
    	});		
    });
    
    window.setTimeout(markVisibleText, 3000);
});

var visible = [];

function markVisibleText() {
    // check for visible spans with class 'track'   
    $('.track').each(function() {
    	
        var pos = $(this).offset(),
            oH = $(this).outerHeight(),
            oW = $(this).outerWidth(),
            wX = $(window).scrollLeft(),
            wY = $(window).scrollTop(),
            wH = $(window).height(),
            wW = $(window).width();
        
        //$(this).css('background-color', '');
        
        if(oH == 0) {
        	return;
        }

        // check the edges
        if ( ( pos.left >= wX ) && 
        		( pos.top >= wY ) && 
        		( oW + pos.left <= wX + wW ) && 
        		( oH + pos.top <= wY + wH ) ) {

            // fully visible
        	var counter = $(this).attr('counter');

            //$(this).css('background-color', 'red');
            
            var minY = (pos.top - wY) / wH;
        	var maxY = minY + oH / wH;   
        	
        	var attention = calculateAttention(minY, maxY); 
        	
        	var acc = visible[counter];
        	acc = acc ? acc + attention : attention;
        	visible[counter] = acc;
        	
        	console.log( counter + "\t" + attention + "\t" + $(this).text());
            
        } else if ( ( ( ( pos.left <= wX ) && ( pos.left + oW > wX ) ) || 
        				( ( pos.left >= wX ) && ( pos.left <= wX + wW ) ) ) && 
        		( ( ( pos.top <= wY ) && ( pos.top + oH > wY ) ) || 
        				( ( pos.top >= wY ) && ( pos.top <= wY + wH ) ) ) ) {

        	// partially visible
        	var counter = $(this).attr('counter');

            //$(this).css('background-color', 'red');
            
            var minY = (pos.top - wY) / wH;
        	var maxY = minY + oH / wH;   
        	
        	var attention = calculateAttention(minY, maxY); 
        	
        	var acc = visible[counter];
        	acc = acc ? acc + attention : attention;
        	visible[counter] = acc;
        	
        	console.log( counter + "\t" + attention + "\t" + $(this).text());
            
        }
    });
    
    window.setTimeout(markVisibleText, 500);
}

function calculateAttention(from, to) {
	var x = (from + to) / 2;
	var mostAttention = 0.4;
	return Math.exp(-(x-mostAttention) * (x-mostAttention) * 8);
}