var visible = [];

var displayingDistribution = false;

$(document).ready(function() {
	
	if(typeof document.track_initialized !== 'undefined') {
		return;
	}
	
	document.track_initialized = true;
	
	$('.track-button').each(function(i, obj) {
		if( i > 0) {
			$(this).remove();
		}
	});
    
	$('.track-button').click(function(){
		
		if(displayingDistribution) {
			$('.track').each(function() {
				$(this).css('background-color', '');
			});
			displayingDistribution = false;
			return;
		} else {
			displayingDistribution = true;
		}
    	
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

function markVisibleText() {
	
	if(displayingDistribution || (!isTabVisible())) {
		window.setTimeout(markVisibleText, 500);
		return;
	}
	
	
    // check for visible spans with class 'track'   
    $('.track').each(function() {
    	
        var pos = $(this).offset(),
            oH = $(this).outerHeight(),
            oW = $(this).outerWidth(),
            wX = $(window).scrollLeft(),
            wY = $(window).scrollTop(),
            wH = $(window).height(),
            wW = $(window).width();
        
        if(oH == 0) {
        	return;
        }


        if ( // TODO: refactoring
                // fully visible		
        		( ( pos.left >= wX ) && 
        		( pos.top >= wY ) && 
        		( oW + pos.left <= wX + wW ) && 
        		( oH + pos.top <= wY + wH ) )
        	||
        		// partially visible
        		( ( ( ( ( pos.left <= wX ) && ( pos.left + oW > wX ) ) || 
        		( ( pos.left >= wX ) && ( pos.left <= wX + wW ) ) ) && 
                ( ( ( pos.top <= wY ) && ( pos.top + oH > wY ) ) || 
                ( ( pos.top >= wY ) && ( pos.top <= wY + wH ) ) ) ) )
        	) {
	        	var counter = $(this).attr('counter');
	            
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

// http://stackoverflow.com/questions/19519535/detect-if-browser-tab-is-active-or-user-has-switched-away/19519701#19519701
var isTabVisible = (function(){
    var stateKey, eventKey, keys = {
        hidden: "visibilitychange",
        webkitHidden: "webkitvisibilitychange",
        mozHidden: "mozvisibilitychange",
        msHidden: "msvisibilitychange"
    };
    for (stateKey in keys) {
        if (stateKey in document) {
            eventKey = keys[stateKey];
            break;
        }
    }
    return function(c) {
        if (c) document.addEventListener(eventKey, c);
        return !document[stateKey];
    }
})();