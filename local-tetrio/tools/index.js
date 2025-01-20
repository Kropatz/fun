const msgpackr = require('msgpackr');

msgpackr.addExtension({
  type: 1,
  read: e => null === e ? {success: !0} : {success: !0, ...e}
})

msgpackr.addExtension(
    {type: 2, read: e => null === e ? {success: !1} : {success: !1, error: e}})
let unpackr = new msgpackr.Unpackr({
  bundleStrings: !0,
});

// read the data from file:
let packed = require('fs').readFileSync('environment_response');

let data = unpackr.unpack(packed)
console.log(data);
