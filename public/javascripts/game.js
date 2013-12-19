$(document).ready(function() {
	
	connectWebSocketAndSetUpPushHandler();
	setUpClickHandler();
	$(window).resize(scaleGameBoard);
	$(window).resize();
	$('body').backstretch("/assets/images/body.jpg");
	
});


function connectWebSocketAndSetUpPushHandler() {
	var socketUrl = 'ws://' + window.location.host + '/socket';
	var socket = new WebSocket(socketUrl);
	socket.onmessage = function(msg) {
		var jsonMsg = JSON.parse(msg.data);
		refreshGameFromJsonData(jsonMsg);
	};
}

function refreshGameFromJsonData(data) {
	refreshStatus(data.status, data.activePlayer);
	refreshControlButtons(data.canStartGame, data.canQuitGame, data.canAddPlayer);
	refreshDiceContainer(data.dice, data.activePlayer);
	clearAllFields();
	refreshSpecialFields(data.homeFields, 'home');
	refreshSpecialFields(data.finishFields, 'finish');
	refreshPublicFields(data.publicFields);
}

function clearAllFields() {
	$('.figure').attr('class', 'figure').attr('id', '').attr('href', '#');
	$('.player-name').html('');
}

function refreshStatus(status, activePlayer) {
	$('#status-text .body').fadeOut(function() {
		if (activePlayer) {
			$(this).find('.player span').attr('class', 'player-'+activePlayer.id)
				.html(activePlayer.name + ':');
		}
		$(this).find('.text').html(status);
		$(this).fadeIn();
	});
}

function refreshControlButtons(canStart, canQuit, canAddPlayer) {
	var start = $('#start');
	var quit = $('#quit');
	var addPlayer = $('#add-player');
	
	if (canStart) {
		start.fadeIn();
	} else {
		start.hide();
	}
	if (canQuit) {
		quit.fadeIn();
	} else {
		quit.hide();
	}
	if (canAddPlayer) {
		addPlayer.fadeIn();
	} else {
		addPlayer.hide();
	}
}

function changeDice(number, delay) {
	setTimeout(function() {
		$("#dice").attr('class', 'show-' + number);
	}, delay);
};

function refreshDiceContainer(dice, activePlayer) {
	var maxPlayers = 4;
	for (var i = 0; i < maxPlayers; i++) {
		$('.dice-container').removeClass('player-' + i);
	}

	if (activePlayer) {
		$('.dice-container').addClass('player-' + activePlayer.id);
	}
	
	if (dice) {
		var numbers = new Array();
		numbers[1] = "one";
		numbers[2] = "two";
		numbers[3] = "three";
		numbers[4] = "four";
		numbers[5] = "five";
		numbers[6] = "six";
		
		// spin the dice
		changeDice(numbers[1], 0);
		changeDice(numbers[3], 300);
		changeDice(numbers[5], 600);
		// final number
		changeDice(numbers[dice], 900);
	}
}

function attachFigureAttrsToLink(link, figureLetter, playerId) {
	link.attr('href', '/cmd/m:' + figureLetter);
	link.addClass('player-' + playerId);
	link.attr('id', 'figure-' + figureLetter);
}


function refreshSpecialFields(specialFields, type) {
	specialFields.forEach(function(specialFieldEntry) {
		
		// set name to each special field
		$('.player-name.player-'+specialFieldEntry.playerId).html(specialFieldEntry.playerName);
		
		var specialField = $('.'+type+'.player-'+specialFieldEntry.playerId);
		
		specialFieldEntry.fields.forEach(function(field) {
			var occupiedField = specialField.find('.field-'+field.index+' .figure');
			attachFigureAttrsToLink(occupiedField, field.figure, specialFieldEntry.playerId);
		});
		
	});
}

function refreshPublicFields(publicFields) {
	publicFields.forEach(function(field) {
		var occupiedField = $('.public .field-'+field.index+' .figure');
		attachFigureAttrsToLink(occupiedField, field.figure, field.playerId);
	});
}

function scaleGameBoard() {
	var fieldSmall = 30;
	var fieldLarge = 45;
	var diceSmall = 40;
	var diceLarge = 60;
	var fieldSize = fieldSmall;
	var diceSize = diceSmall;
	var diceMargin = (fieldSize - diceSize) / 2;
	
	var gameboard = $("#gameboard");
	
	var maxWidth = $(window).height() - $("#control").height();
	$(gameboard).css("max-width", maxWidth);
	
	if ($(gameboard).width() >= 700) {
		fieldSize = fieldLarge;
		diceSize = diceLarge;
		diceMargin = (fieldSize - diceSize) / 2;
	}
	
	$('.field').css({
		'width': fieldSize,
		'height': fieldSize
	});
	
	$('.dice-container').css({
		'margin-top': diceMargin,
		'margin-left': diceMargin
	});
	$('.dice-container, #dice .side').css({
		'width': diceSize,
		'height': diceSize
	});
	
	$('#dice .side').css({
		'font-size': diceSize * 0.6,
		'line-height': diceSize + 'px'
	});
}

function setUpClickHandler() {
	$('#add-player-dialog').on('shown.bs.modal', function() {
		$(this).find('input').focus();
	});
	
	$('#add-player-dialog input').keypress(function(e) {
		if (e.which == 13) {
			$('#add-player-dialog .btn-primary').click();
		}
	});
	
	$('#add-player-dialog .btn-primary').click(function(e) {
		var input = $('#add-player-dialog input');
		var name = input.val();
		if (name) {
			$.get('/api/tui/add:' + name);
			input.val('');
			$('#add-player-dialog').modal('hide');
		}
	});
	
	$('#start').click(function(e) {
		$.get('/api/tui/s');
		e.preventDefault();
	});
	
	$('#quit').click(function(e) {
		$.get('/api/tui/r');
		e.preventDefault();
	});
	
	$('.figure').click(function(e) {
		var figure = $(this).attr("id").split("-")[1];
		$.get('/api/tui/m:' + figure);
		e.preventDefault();
	});
	
	$('.dice-container').click(function(e) {
		$.get("/api/tui/d");
	});
}
