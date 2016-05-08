
/**
 * The riot template engine
 * @version v2.4.0
 */
/**
 * riot.util.brackets
 *
 * - `brackets    ` - Returns a string or regex based on its parameter
 * - `brackets.set` - Change the current riot brackets
 *
 * @module
 */

var brackets = (function (UNDEF) {

  var
    REGLOB = 'g',

    R_MLCOMMS = /\/\*[^*]*\*+(?:[^*\/][^*]*\*+)*\//g,

    R_STRINGS = /"[^"\\]*(?:\\[\S\s][^"\\]*)*"|'[^'\\]*(?:\\[\S\s][^'\\]*)*'/g,

    S_QBLOCKS = R_STRINGS.source + '|' +
      /(?:\breturn\s+|(?:[$\w\)\]]|\+\+|--)\s*(\/)(?![*\/]))/.source + '|' +
      /\/(?=[^*\/])[^[\/\\]*(?:(?:\[(?:\\.|[^\]\\]*)*\]|\\.)[^[\/\\]*)*?(\/)[gim]*/.source,

    FINDBRACES = {
      '(': RegExp('([()])|'   + S_QBLOCKS, REGLOB),
      '[': RegExp('([[\\]])|' + S_QBLOCKS, REGLOB),
      '{': RegExp('([{}])|'   + S_QBLOCKS, REGLOB)
    },

    DEFAULT = '{ }'

  var _pairs = [
    '{', '}',
    '{', '}',
    /{[^}]*}/,
    /\\([{}])/g,
    /\\({)|{/g,
    RegExp('\\\\(})|([[({])|(})|' + S_QBLOCKS, REGLOB),
    DEFAULT,
    /^\s*{\^?\s*([$\w]+)(?:\s*,\s*(\S+))?\s+in\s+(\S.*)\s*}/,
    /(^|[^\\]){=[\S\s]*?}/
  ]

  var
    cachedBrackets = UNDEF,
    _regex,
    _cache = [],
    _settings

  function _loopback (re) { return re }

  function _rewrite (re, bp) {
    if (!bp) bp = _cache
    return new RegExp(
      re.source.replace(/{/g, bp[2]).replace(/}/g, bp[3]), re.global ? REGLOB : ''
    )
  }

  function _create (pair) {
    if (pair === DEFAULT) return _pairs

    var arr = pair.split(' ')

    if (arr.length !== 2 || /[\x00-\x1F<>a-zA-Z0-9'",;\\]/.test(pair)) { // eslint-disable-line
      throw new Error('Unsupported brackets "' + pair + '"')
    }
    arr = arr.concat(pair.replace(/(?=[[\]()*+?.^$|])/g, '\\').split(' '))

    arr[4] = _rewrite(arr[1].length > 1 ? /{[\S\s]*?}/ : _pairs[4], arr)
    arr[5] = _rewrite(pair.length > 3 ? /\\({|})/g : _pairs[5], arr)
    arr[6] = _rewrite(_pairs[6], arr)
    arr[7] = RegExp('\\\\(' + arr[3] + ')|([[({])|(' + arr[3] + ')|' + S_QBLOCKS, REGLOB)
    arr[8] = pair
    return arr
  }

  function _brackets (reOrIdx) {
    return reOrIdx instanceof RegExp ? _regex(reOrIdx) : _cache[reOrIdx]
  }

  _brackets.split = function split (str, tmpl, _bp) {
    // istanbul ignore next: _bp is for the compiler
    if (!_bp) _bp = _cache

    var
      parts = [],
      match,
      isexpr,
      start,
      pos,
      re = _bp[6]

    isexpr = start = re.lastIndex = 0

    while ((match = re.exec(str))) {

      pos = match.index

      if (isexpr) {

        if (match[2]) {
          re.lastIndex = skipBraces(str, match[2], re.lastIndex)
          continue
        }
        if (!match[3]) {
          continue
        }
      }

      if (!match[1]) {
        unescapeStr(str.slice(start, pos))
        start = re.lastIndex
        re = _bp[6 + (isexpr ^= 1)]
        re.lastIndex = start
      }
    }

    if (str && start < str.length) {
      unescapeStr(str.slice(start))
    }

    return parts

    function unescapeStr (s) {
      if (tmpl || isexpr) {
        parts.push(s && s.replace(_bp[5], '$1'))
      } else {
        parts.push(s)
      }
    }

    function skipBraces (s, ch, ix) {
      var
        match,
        recch = FINDBRACES[ch]

      recch.lastIndex = ix
      ix = 1
      while ((match = recch.exec(s))) {
        if (match[1] &&
          !(match[1] === ch ? ++ix : --ix)) break
      }
      return ix ? s.length : recch.lastIndex
    }
  }

  _brackets.hasExpr = function hasExpr (str) {
    return _cache[4].test(str)
  }

  _brackets.loopKeys = function loopKeys (expr) {
    var m = expr.match(_cache[9])

    return m
      ? { key: m[1], pos: m[2], val: _cache[0] + m[3].trim() + _cache[1] }
      : { val: expr.trim() }
  }

  _brackets.array = function array (pair) {
    return pair ? _create(pair) : _cache
  }

  function _reset (pair) {
    if ((pair || (pair = DEFAULT)) !== _cache[8]) {
      _cache = _create(pair)
      _regex = pair === DEFAULT ? _loopback : _rewrite
      _cache[9] = _regex(_pairs[9])
    }
    cachedBrackets = pair
  }

  function _setSettings (o) {
    var b

    o = o || {}
    b = o.brackets
    Object.defineProperty(o, 'brackets', {
      set: _reset,
      get: function () { return cachedBrackets },
      enumerable: true
    })
    _settings = o
    _reset(b)
  }

  Object.defineProperty(_brackets, 'settings', {
    set: _setSettings,
    get: function () { return _settings }
  })

  /* istanbul ignore next: in the browser riot is always in the scope */
  _brackets.settings = typeof riot !== 'undefined' && riot.settings || {}
  _brackets.set = _reset

  _brackets.R_STRINGS = R_STRINGS
  _brackets.R_MLCOMMS = R_MLCOMMS
  _brackets.S_QBLOCKS = S_QBLOCKS

  return _brackets

})()

/**
 * @module tmpl
 *
 * tmpl          - Root function, returns the template value, render with data
 * tmpl.hasExpr  - Test the existence of a expression inside a string
 * tmpl.loopKeys - Get the keys for an 'each' loop (used by `_each`)
 */

var tmpl = (function () {

  var _cache = {}

  function _tmpl (str, data) {
    if (!str) return str

    return (_cache[str] || (_cache[str] = _create(str))).call(data, _logErr)
  }

  _tmpl.haveRaw = brackets.hasRaw

  _tmpl.hasExpr = brackets.hasExpr

  _tmpl.loopKeys = brackets.loopKeys

  _tmpl.errorHandler = null

  function _logErr (err, ctx) {

    if (_tmpl.errorHandler) {

      err.riotData = {
        tagName: ctx && ctx.root && ctx.root.tagName,
        _riot_id: ctx && ctx._riot_id  //eslint-disable-line camelcase
      }
      _tmpl.errorHandler(err)
    }
  }

  function _create (str) {
    var expr = _getTmpl(str)

    if (expr.slice(0, 11) !== 'try{return ') expr = 'return ' + expr

/* eslint-disable */

    return new Function('E', expr + ';')
/* eslint-enable */
  }

  var
    CH_IDEXPR = '\u2057',
    RE_CSNAME = /^(?:(-?[_A-Za-z\xA0-\xFF][-\w\xA0-\xFF]*)|\u2057(\d+)~):/,
    RE_QBLOCK = RegExp(brackets.S_QBLOCKS, 'g'),
    RE_DQUOTE = /\u2057/g,
    RE_QBMARK = /\u2057(\d+)~/g

  function _getTmpl (str) {
    var
      qstr = [],
      expr,
      parts = brackets.split(str.replace(RE_DQUOTE, '"'), 1)

    if (parts.length > 2 || parts[0]) {
      var i, j, list = []

      for (i = j = 0; i < parts.length; ++i) {

        expr = parts[i]

        if (expr && (expr = i & 1

            ? _parseExpr(expr, 1, qstr)

            : '"' + expr
                .replace(/\\/g, '\\\\')
                .replace(/\r\n?|\n/g, '\\n')
                .replace(/"/g, '\\"') +
              '"'

          )) list[j++] = expr

      }

      expr = j < 2 ? list[0]
           : '[' + list.join(',') + '].join("")'

    } else {

      expr = _parseExpr(parts[1], 0, qstr)
    }

    if (qstr[0]) {
      expr = expr.replace(RE_QBMARK, function (_, pos) {
        return qstr[pos]
          .replace(/\r/g, '\\r')
          .replace(/\n/g, '\\n')
      })
    }
    return expr
  }

  var
    RE_BREND = {
      '(': /[()]/g,
      '[': /[[\]]/g,
      '{': /[{}]/g
    }

  function _parseExpr (expr, asText, qstr) {

    expr = expr
          .replace(RE_QBLOCK, function (s, div) {
            return s.length > 2 && !div ? CH_IDEXPR + (qstr.push(s) - 1) + '~' : s
          })
          .replace(/\s+/g, ' ').trim()
          .replace(/\ ?([[\({},?\.:])\ ?/g, '$1')

    if (expr) {
      var
        list = [],
        cnt = 0,
        match

      while (expr &&
            (match = expr.match(RE_CSNAME)) &&
            !match.index
        ) {
        var
          key,
          jsb,
          re = /,|([[{(])|$/g

        expr = RegExp.rightContext
        key  = match[2] ? qstr[match[2]].slice(1, -1).trim().replace(/\s+/g, ' ') : match[1]

        while (jsb = (match = re.exec(expr))[1]) skipBraces(jsb, re)

        jsb  = expr.slice(0, match.index)
        expr = RegExp.rightContext

        list[cnt++] = _wrapExpr(jsb, 1, key)
      }

      expr = !cnt ? _wrapExpr(expr, asText)
           : cnt > 1 ? '[' + list.join(',') + '].join(" ").trim()' : list[0]
    }
    return expr

    function skipBraces (ch, re) {
      var
        mm,
        lv = 1,
        ir = RE_BREND[ch]

      ir.lastIndex = re.lastIndex
      while (mm = ir.exec(expr)) {
        if (mm[0] === ch) ++lv
        else if (!--lv) break
      }
      re.lastIndex = lv ? expr.length : ir.lastIndex
    }
  }

  // istanbul ignore next: not both
  var // eslint-disable-next-line max-len
    JS_CONTEXT = '"in this?this:' + (typeof window !== 'object' ? 'global' : 'window') + ').',
    JS_VARNAME = /[,{][$\w]+:|(^ *|[^$\w\.])(?!(?:typeof|true|false|null|undefined|in|instanceof|is(?:Finite|NaN)|void|NaN|new|Date|RegExp|Math)(?![$\w]))([$_A-Za-z][$\w]*)/g,
    JS_NOPROPS = /^(?=(\.[$\w]+))\1(?:[^.[(]|$)/

  function _wrapExpr (expr, asText, key) {
    var tb

    expr = expr.replace(JS_VARNAME, function (match, p, mvar, pos, s) {
      if (mvar) {
        pos = tb ? 0 : pos + match.length

        if (mvar !== 'this' && mvar !== 'global' && mvar !== 'window') {
          match = p + '("' + mvar + JS_CONTEXT + mvar
          if (pos) tb = (s = s[pos]) === '.' || s === '(' || s === '['
        } else if (pos) {
          tb = !JS_NOPROPS.test(s.slice(pos))
        }
      }
      return match
    })

    if (tb) {
      expr = 'try{return ' + expr + '}catch(e){E(e,this)}'
    }

    if (key) {

      expr = (tb
          ? 'function(){' + expr + '}.call(this)' : '(' + expr + ')'
        ) + '?"' + key + '":""'

    } else if (asText) {

      expr = 'function(v){' + (tb
          ? expr.replace('return ', 'v=') : 'v=(' + expr + ')'
        ) + ';return v||v===0?v:""}.call(this)'
    }

    return expr
  }

  // istanbul ignore next: compatibility fix for beta versions
  _tmpl.parse = function (s) { return s }

  _tmpl.version = brackets.version = 'v2.4.0'

  return _tmpl

})();


/**
 * @module parsers
 */
var parsers = (function () {

  function _req (name) {
    var parser = window[name]

    if (parser) return parser

    throw new Error(name + ' parser not found.')
  }

  function extend (obj, props) {
    if (props) {
      for (var prop in props) {
        /* istanbul ignore next */
        if (props.hasOwnProperty(prop)) {
          obj[prop] = props[prop]
        }
      }
    }
    return obj
  }

  function renderPug (compilerName, html, opts, url) {
    opts = extend({
      pretty: true,
      filename: url,
      doctype: 'html'
    }, opts)
    return _req(compilerName).render(html, opts)
  }

  var _p = {
    html: {
      jade: function (html, opts, url) {
        /* eslint-disable */
        console.log('DEPRECATION WARNING: jade was renamed "pug" - the jade parser will be removed in riot@3.0.0!')
        /* eslint-enable */
        return renderPug('jade', html, opts, url)
      },
      pug: function (html, opts, url) {
        return renderPug('pug', html, opts, url)
      }
    },

    css: {
      less: function (tag, css, opts, url) {
        var ret

        opts = extend({
          sync: true,
          syncImport: true,
          filename: url
        }, opts)
        _req('less').render(css, opts, function (err, result) {
          // istanbul ignore next
          if (err) throw err
          ret = result.css
        })
        return ret
      }
    },

    js: {
      es6: function (js, opts) {
        opts = extend({
          blacklist: ['useStrict', 'strict', 'react'],
          sourceMaps: false,
          comments: false
        }, opts)
        return _req('babel').transform(js, opts).code
      },
      babel: function (js, opts, url) {
        return _req('babel').transform(js, extend({ filename: url }, opts)).code
      },
      coffee: function (js, opts) {
        return _req('CoffeeScript').compile(js, extend({ bare: true }, opts))
      },
      livescript: function (js, opts) {
        return _req('livescript').compile(js, extend({ bare: true, header: false }, opts))
      },
      typescript: function (js, opts) {
        return _req('typescript')(js, opts)
      },
      none: function (js) {
        return js
      }
    }
  }

  _p.js.javascript   = _p.js.none
  _p.js.coffeescript = _p.js.coffee

  _p.utils = {
    extend: extend
  }

  return _p

})()

riot.parsers = parsers

/**
 * Compiler for riot custom tags
 * @version v2.4.1
 */
var compile = (function () {

  /* eslint-disable */

  var extend = parsers.utils.extend
  /* eslint-enable */

  var S_LINESTR = /"[^"\n\\]*(?:\\[\S\s][^"\n\\]*)*"|'[^'\n\\]*(?:\\[\S\s][^'\n\\]*)*'/.source

  var S_STRINGS = brackets.R_STRINGS.source

  var HTML_ATTRS = / *([-\w:\xA0-\xFF]+) ?(?:= ?('[^']*'|"[^"]*"|\S+))?/g

  var HTML_COMMS = RegExp(/<!--(?!>)[\S\s]*?-->/.source + '|' + S_LINESTR, 'g')

  var HTML_TAGS = /<(-?[A-Za-z][-\w\xA0-\xFF]*)(?:\s+([^"'\/>]*(?:(?:"[^"]*"|'[^']*'|\/[^>])[^'"\/>]*)*)|\s*)(\/?)>/g

  var HTML_PACK = />[ \t]+<(-?[A-Za-z]|\/[-A-Za-z])/g

  var BOOL_ATTRS = RegExp(
      '^(?:disabled|checked|readonly|required|allowfullscreen|auto(?:focus|play)|' +
      'compact|controls|default|formnovalidate|hidden|ismap|itemscope|loop|' +
      'multiple|muted|no(?:resize|shade|validate|wrap)?|open|reversed|seamless|' +
      'selected|sortable|truespeed|typemustmatch)$')

  var RIOT_ATTRS = ['style', 'src', 'd']

  var VOID_TAGS = /^(?:input|img|br|wbr|hr|area|base|col|embed|keygen|link|meta|param|source|track)$/

  var PRE_TAGS = /<pre(?:\s+(?:[^">]*|"[^"]*")*)?>([\S\s]+?)<\/pre\s*>/gi

  var SPEC_TYPES = /^"(?:number|date(?:time)?|time|month|email|color)\b/i

  var TRIM_TRAIL = /[ \t]+$/gm

  var
    RE_HASEXPR = /\x01#\d/,
    RE_REPEXPR = /\x01#(\d+)/g,
    CH_IDEXPR  = '\x01#',
    CH_DQCODE  = '\u2057',
    DQ = '"',
    SQ = "'"

  function cleanSource (src) {
    var
      mm,
      re = HTML_COMMS

    if (~src.indexOf('\r')) {
      src = src.replace(/\r\n?/g, '\n')
    }

    re.lastIndex = 0
    while ((mm = re.exec(src))) {
      if (mm[0][0] === '<') {
        src = RegExp.leftContext + RegExp.rightContext
        re.lastIndex = mm[3] + 1
      }
    }
    return src
  }

  function parseAttribs (str, pcex) {
    var
      list = [],
      match,
      type, vexp

    HTML_ATTRS.lastIndex = 0

    str = str.replace(/\s+/g, ' ')

    while ((match = HTML_ATTRS.exec(str))) {
      var
        k = match[1].toLowerCase(),
        v = match[2]

      if (!v) {
        list.push(k)
      } else {

        if (v[0] !== DQ) {
          v = DQ + (v[0] === SQ ? v.slice(1, -1) : v) + DQ
        }

        if (k === 'type' && SPEC_TYPES.test(v)) {
          type = v
        } else {
          if (RE_HASEXPR.test(v)) {

            if (k === 'value') vexp = 1
            else if (BOOL_ATTRS.test(k)) k = '__' + k
            else if (~RIOT_ATTRS.indexOf(k)) k = 'riot-' + k
          }

          list.push(k + '=' + v)
        }
      }
    }

    if (type) {
      if (vexp) type = DQ + pcex._bp[0] + SQ + type.slice(1, -1) + SQ + pcex._bp[1] + DQ
      list.push('type=' + type)
    }
    return list.join(' ')
  }

  function splitHtml (html, opts, pcex) {
    var _bp = pcex._bp

    if (html && _bp[4].test(html)) {
      var
        jsfn = opts.expr && (opts.parser || opts.type) ? _compileJS : 0,
        list = brackets.split(html, 0, _bp),
        expr

      for (var i = 1; i < list.length; i += 2) {
        expr = list[i]
        if (expr[0] === '^') {
          expr = expr.slice(1)
        } else if (jsfn) {
          expr = jsfn(expr, opts).trim()
          if (expr.slice(-1) === ';') expr = expr.slice(0, -1)
        }
        list[i] = CH_IDEXPR + (pcex.push(expr) - 1) + _bp[1]
      }
      html = list.join('')
    }
    return html
  }

  function restoreExpr (html, pcex) {
    if (pcex.length) {
      html = html.replace(RE_REPEXPR, function (_, d) {

        return pcex._bp[0] + pcex[d].trim().replace(/[\r\n]+/g, ' ').replace(/"/g, CH_DQCODE)
      })
    }
    return html
  }

  function _compileHTML (html, opts, pcex) {

    html = splitHtml(html, opts, pcex)
      .replace(HTML_TAGS, function (_, name, attr, ends) {

        name = name.toLowerCase()

        ends = ends && !VOID_TAGS.test(name) ? '></' + name : ''

        if (attr) name += ' ' + parseAttribs(attr, pcex)

        return '<' + name + ends + '>'
      })

    if (!opts.whitespace) {
      var p = []

      if (/<pre[\s>]/.test(html)) {
        html = html.replace(PRE_TAGS, function (q) {
          p.push(q)
          return '\u0002'
        })
      }

      html = html.trim().replace(/\s+/g, ' ')

      if (p.length) html = html.replace(/\u0002/g, function () { return p.shift() })
    }

    if (opts.compact) html = html.replace(HTML_PACK, '><$1')

    return restoreExpr(html, pcex).replace(TRIM_TRAIL, '')
  }

  function compileHTML (html, opts, pcex) {

    if (Array.isArray(opts)) {
      pcex = opts
      opts = {}
    } else {
      if (!pcex) pcex = []
      if (!opts) opts = {}
    }

    pcex._bp = brackets.array(opts.brackets)

    return _compileHTML(cleanSource(html), opts, pcex)
  }

  var JS_ES6SIGN = /^[ \t]*([$_A-Za-z][$\w]*)\s*\([^()]*\)\s*{/m

  var JS_ES6END = RegExp('[{}]|' + brackets.S_QBLOCKS, 'g')

  var JS_COMMS = RegExp(brackets.R_MLCOMMS.source + '|//[^\r\n]*|' + brackets.S_QBLOCKS, 'g')

  function riotjs (js) {
    var
      parts = [],
      match,
      toes5,
      pos,
      name,
      RE = RegExp

    if (~js.indexOf('/')) js = rmComms(js, JS_COMMS)

    while ((match = js.match(JS_ES6SIGN))) {

      parts.push(RE.leftContext)
      js  = RE.rightContext
      pos = skipBody(js, JS_ES6END)

      name  = match[1]
      toes5 = !/^(?:if|while|for|switch|catch|function)$/.test(name)
      name  = toes5 ? match[0].replace(name, 'this.' + name + ' = function') : match[0]
      parts.push(name, js.slice(0, pos))
      js = js.slice(pos)

      if (toes5 && !/^\s*.\s*bind\b/.test(js)) parts.push('.bind(this)')
    }

    return parts.length ? parts.join('') + js : js

    function rmComms (s, r, m) {
      r.lastIndex = 0
      while ((m = r.exec(s))) {
        if (m[0][0] === '/' && !m[1] && !m[2]) {
          s = RE.leftContext + ' ' + RE.rightContext
          r.lastIndex = m[3] + 1
        }
      }
      return s
    }

    function skipBody (s, r) {
      var m, i = 1

      r.lastIndex = 0
      while (i && (m = r.exec(s))) {
        if (m[0] === '{') ++i
        else if (m[0] === '}') --i
      }
      return i ? s.length : r.lastIndex
    }
  }

  function _compileJS (js, opts, type, parserOpts, url) {
    if (!/\S/.test(js)) return ''
    if (!type) type = opts.type

    var parser = opts.parser || (type ? parsers.js[type] : riotjs)

    if (!parser) {
      throw new Error('JS parser not found: "' + type + '"')
    }
    return parser(js, parserOpts, url).replace(/\r\n?/g, '\n').replace(TRIM_TRAIL, '')
  }

  function compileJS (js, opts, type, userOpts) {
    if (typeof opts === 'string') {
      userOpts = type
      type = opts
      opts = {}
    }
    if (type && typeof type === 'object') {
      userOpts = type
      type = ''
    }
    if (!userOpts) userOpts = {}

    return _compileJS(js, opts || {}, type, userOpts.parserOptions, userOpts.url)
  }

  var CSS_SELECTOR = RegExp('([{}]|^)[ ;]*([^@ ;{}][^{}]*)(?={)|' + S_LINESTR, 'g')

  function scopedCSS (tag, css) {
    var scope = ':scope'

    return css.replace(CSS_SELECTOR, function (m, p1, p2) {

      if (!p2) return m

      p2 = p2.replace(/[^,]+/g, function (sel) {
        var s = sel.trim()

        if (!s || s === 'from' || s === 'to' || s.slice(-1) === '%') {
          return sel
        }

        if (s.indexOf(scope) < 0) {
          s = tag + ' ' + s + ',[riot-tag="' + tag + '"] ' + s +
                              ',[data-is="' + tag + '"] ' + s
        } else {
          s = s.replace(scope, tag) + ',' +
              s.replace(scope, '[riot-tag="' + tag + '"]') + ',' +
              s.replace(scope, '[data-is="' + tag + '"]')
        }
        return s
      })

      return p1 ? p1 + ' ' + p2 : p2
    })
  }

  function _compileCSS (css, tag, type, opts) {
    var scoped = (opts || (opts = {})).scoped

    if (type) {
      if (type === 'scoped-css') {
        scoped = true
      } else if (parsers.css[type]) {
        css = parsers.css[type](tag, css, opts.parserOpts || {}, opts.url)
      } else if (type !== 'css') {
        throw new Error('CSS parser not found: "' + type + '"')
      }
    }

    css = css.replace(brackets.R_MLCOMMS, '').replace(/\s+/g, ' ').trim()

    if (scoped) {
      if (!tag) {
        throw new Error('Can not parse scoped CSS without a tagName')
      }
      css = scopedCSS(tag, css)
    }
    return css
  }

  function compileCSS (css, type, opts) {
    if (type && typeof type === 'object') {
      opts = type
      type = ''
    } else if (!opts) opts = {}

    return _compileCSS(css, opts.tagName, type, opts)
  }

  var TYPE_ATTR = /\stype\s*=\s*(?:(['"])(.+?)\1|(\S+))/i

  var MISC_ATTR = '\\s*=\\s*(' + S_STRINGS + '|{[^}]+}|\\S+)'

  var END_TAGS = /\/>\n|^<(?:\/?-?[A-Za-z][-\w\xA0-\xFF]*\s*|-?[A-Za-z][-\w\xA0-\xFF]*\s+[-\w:\xA0-\xFF][\S\s]*?)>\n/

  function _q (s, r) {
    if (!s) return "''"
    s = SQ + s.replace(/\\/g, '\\\\').replace(/'/g, "\\'") + SQ
    return r && ~s.indexOf('\n') ? s.replace(/\n/g, '\\n') : s
  }

  function mktag (name, html, css, attr, js, opts) {
    var
      c = opts.debug ? ',\n  ' : ', ',
      s = '});'

    if (js && js.slice(-1) !== '\n') s = '\n' + s

    return 'riot.tag2(\'' + name + SQ +
      c + _q(html, 1) +
      c + _q(css) +
      c + _q(attr) + ', function(opts) {\n' + js + s
  }

  function splitBlocks (str) {
    if (/<[-\w]/.test(str)) {
      var
        m,
        k = str.lastIndexOf('<'),
        n = str.length

      while (~k) {
        m = str.slice(k, n).match(END_TAGS)
        if (m) {
          k += m.index + m[0].length
          return [str.slice(0, k), str.slice(k)]
        }
        n = k
        k = str.lastIndexOf('<', k - 1)
      }
    }
    return ['', str]
  }

  function getType (attribs) {
    if (attribs) {
      var match = attribs.match(TYPE_ATTR)

      match = match && (match[2] || match[3])
      if (match) {
        return match.replace('text/', '')
      }
    }
    return ''
  }

  function getAttrib (attribs, name) {
    if (attribs) {
      var match = attribs.match(RegExp('\\s' + name + MISC_ATTR, 'i'))

      match = match && match[1]
      if (match) {
        return (/^['"]/).test(match) ? match.slice(1, -1) : match
      }
    }
    return ''
  }

  function unescapeHTML (str) {
    return str
            .replace(/&amp;/g, '&')
            .replace(/&lt;/g, '<')
            .replace(/&gt;/g, '>')
            .replace(/&quot;/g, '"')
            .replace(/&#039;/g, '\'')
  }

  function getParserOptions (attribs) {
    var opts = unescapeHTML(getAttrib(attribs, 'options'))

    return opts ? JSON.parse(opts) : null
  }

  function getCode (code, opts, attribs, base) {
    var
      type = getType(attribs),
      src  = getAttrib(attribs, 'src'),
      jsParserOptions = extend({}, opts.parserOptions.js)

    if (src) return false

    return _compileJS(
            code,
            opts,
            type,
            extend(jsParserOptions, getParserOptions(attribs)),
            base
          )
  }

  function cssCode (code, opts, attribs, url, tag) {
    var
      parserStyleOptions = extend({}, opts.parserOptions.style),
      extraOpts = {
        parserOpts: extend(parserStyleOptions, getParserOptions(attribs)),
        scoped: attribs && /\sscoped(\s|=|$)/i.test(attribs),
        url: url
      }

    return _compileCSS(code, tag, getType(attribs) || opts.style, extraOpts)
  }

  function compileTemplate (html, url, lang, opts) {
    var parser = parsers.html[lang]

    if (!parser) {
      throw new Error('Template parser not found: "' + lang + '"')
    }
    return parser(html, opts, url)
  }

  var

    CUST_TAG = RegExp(/^([ \t]*)<(-?[A-Za-z][-\w\xA0-\xFF]*)(?:\s+([^'"\/>]+(?:(?:@|\/[^>])[^'"\/>]*)*)|\s*)?(?:\/>|>[ \t]*\n?([\S\s]*)^\1<\/\2\s*>|>(.*)<\/\2\s*>)/
      .source.replace('@', S_STRINGS), 'gim'),

    SCRIPTS = /<script(\s+[^>]*)?>\n?([\S\s]*?)<\/script\s*>/gi,

    STYLES = /<style(\s+[^>]*)?>\n?([\S\s]*?)<\/style\s*>/gi

  function compile (src, opts, url) {
    var
      parts = [],
      included,
      defaultParserptions = {

        template: {},
        js: {},
        style: {}
      }

    if (!opts) opts = {}

    opts.parserOptions = extend(defaultParserptions, opts.parserOptions || {})

    included = opts.exclude
      ? function (s) { return opts.exclude.indexOf(s) < 0 } : function () { return 1 }

    if (!url) url = ''

    var _bp = brackets.array(opts.brackets)

    if (opts.template) {
      src = compileTemplate(src, url, opts.template, opts.parserOptions.template)
    }

    src = cleanSource(src)
      .replace(CUST_TAG, function (_, indent, tagName, attribs, body, body2) {
        var
          jscode = '',
          styles = '',
          html = '',
          pcex = []

        pcex._bp = _bp

        tagName = tagName.toLowerCase()

        attribs = attribs && included('attribs')
          ? restoreExpr(
              parseAttribs(
                splitHtml(attribs, opts, pcex),
              pcex),
            pcex) : ''

        if ((body || (body = body2)) && /\S/.test(body)) {

          if (body2) {

            if (included('html')) html = _compileHTML(body2, opts, pcex)
          } else {

            body = body.replace(RegExp('^' + indent, 'gm'), '')

            body = body.replace(STYLES, function (_m, _attrs, _style) {
              if (included('css')) {
                styles += (styles ? ' ' : '') + cssCode(_style, opts, _attrs, url, tagName)
              }
              return ''
            })

            body = body.replace(SCRIPTS, function (_m, _attrs, _script) {
              if (included('js')) {
                var code = getCode(_script, opts, _attrs, url)

                if (code) jscode += (jscode ? '\n' : '') + code
              }
              return ''
            })

            var blocks = splitBlocks(body.replace(TRIM_TRAIL, ''))

            if (included('html')) {
              html = _compileHTML(blocks[0], opts, pcex)
            }

            if (included('js')) {
              body = _compileJS(blocks[1], opts, null, null, url)
              if (body) jscode += (jscode ? '\n' : '') + body
            }
          }
        }

        jscode = /\S/.test(jscode) ? jscode.replace(/\n{3,}/g, '\n\n') : ''

        if (opts.entities) {
          parts.push({
            tagName: tagName,
            html: html,
            css: styles,
            attribs: attribs,
            js: jscode
          })
          return ''
        }

        return mktag(tagName, html, styles, attribs, jscode, opts)
      })

    if (opts.entities) return parts

    return src
  }

  riot.util.compiler = {
    compile: compile,
    html: compileHTML,
    css: compileCSS,
    js: compileJS,
    version: 'v2.4.1'
  }
  return compile

})()
