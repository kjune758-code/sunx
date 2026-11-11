export async function api(path, options={}) {
 const response=await fetch('/api'+path,{...options,headers:{'Content-Type':'application/json',...options.headers}});
 const data=await response.json();
 if(!response.ok) throw new Error(data.message||data.detail||`HTTP ${response.status}`);
 return data;
}
export const post=(path,data)=>api(path,{method:'POST',body:JSON.stringify(data)});
