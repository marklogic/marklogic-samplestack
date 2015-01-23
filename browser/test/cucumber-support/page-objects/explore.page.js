var utilities = require('../utilities');

function ExplorePage () {
  var self = this;
  ExplorePage.super_.call(self);
  self.url = '/';

  require('./directives/searchBar.dctv').support(self);
  require('./directives/searchResults.dctv').support(self);
  require('./dialogs/contributor.dlg').support(self);

  self.filters = {
    clearAll: function () {
      return self.qself(q.all([
        self.filters.mineOnly.setValue(false),
        self.filters.resolvedOnly.setValue(false),
        self.filters.dateFrom.setValue(''),
        self.filters.dateTo.setValue('')
      ]));
    },
    mineOnly: {
      setValue: function (value) {
        return self.qself(
          utilities.setCheckboxValue(getMineOnlyFilterElement(), value)
        );
      }
    },
    resolvedOnly: {
      setValue: function (value) {
        return self.qself(
          utilities.setCheckboxValue(getResolvedOnlyFilterElement(), value)
        );
      }
    },
    dateFrom: {
      setValue: function (value) {
        return self.qself(
          utilities.setCheckboxValue(getDateStartFilterElement(), value)
        );
      }
    },
    dateTo: {
      setValue: function (value) {
        return self.qself(
          utilities.setCheckboxValue(getDateEndFilterElement(), value)
        );
      }
    }
  };


  /*******************************/
  /********** PRIVATE ************/
  /*******************************/

  var getMineOnlyFilterElement = function () {
    return element(by.model('showMineOnly'));
  };

  var getResolvedOnlyFilterElement = function () {
    return element(by.model('resolvedOnly'));
  };

  var getDateStartFilterElement = function () {
    return element(by.model('pickerDateStart'));
  };

  var getDateEndFilterElement = function () {
    return element(by.model('pickerDateEnd'));
  };

}

var me = ExplorePage;
me.pageName = 'explore';
me.aliases = ['search', 'default', 'landing'];
World.addPage(me);
